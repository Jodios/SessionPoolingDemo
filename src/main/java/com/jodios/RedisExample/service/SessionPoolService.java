package com.jodios.RedisExample.service;

import com.jodios.RedisExample.config.Config;
import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.util.SessionUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

@Log4j2
@Service
public class SessionPoolService implements DisposableBean {

    private final String SESSION_QUEUE = "QUEUE";
    private final BlockingDeque<SessionCache> sessionQueue;
    private final Map<String, BlockingDeque<SessionCache>> eprToQueue;
    private final SabreSessionService sessionService;
    private final SessionUtils utils;
    private final Config config;

    @Autowired
    public SessionPoolService(SessionUtils utils, SabreSessionService sessionService, Config config){
        this.utils = utils;
        this.config = config;
        this.sessionService = sessionService;
        this.sessionQueue = new LinkedBlockingDeque<>(config.getMaxSessions());
        this.eprToQueue = new ConcurrentHashMap<>();
    }

    public void addToPool(final String epr, SessionCache session) throws InterruptedException {
        log.info("Adding session to {}", epr);
        if(eprToQueue.containsKey(epr)){
            eprToQueue.get(epr).putFirst(session);
        }else{
            BlockingDeque<SessionCache> newQueue = new LinkedBlockingDeque<>(config.getMaxSessions());
            newQueue.putFirst(session);
            eprToQueue.put(epr, newQueue);
        }
    }

    public SessionCache getFromPool(final String epr) throws Exception {
        log.info("Getting session from {}", epr);
        if(eprToQueue.containsKey(epr)){
            BlockingDeque<SessionCache> sessionQueue = eprToQueue.get(epr);
            SessionCache session = sessionQueue.isEmpty() ? sessionService.getNewSession() : sessionQueue.takeFirst();
            while(utils.sessionHasBeenIdleForTooLong(session)){
                if(sessionQueue.isEmpty()){
                    return sessionService.getNewSession();
                }
                session = sessionQueue.takeFirst();
            }
            return session;
        }
        BlockingDeque<SessionCache> newQueue = new LinkedBlockingDeque<>(config.getMaxSessions());
        eprToQueue.put(epr, newQueue);
        return sessionService.getNewSession();
    }

    public BlockingDeque<SessionCache> findByEPR(final String epr){
        return eprToQueue.get(epr);
    }

    public Map<String, BlockingDeque<SessionCache>> findAll(){
        return eprToQueue;
    }

    public void delete(String epr){
        eprToQueue.remove(epr);
    }

    @Override
    public void destroy() throws Exception {
        // TODO: When session is closed then close all active sessions
        System.out.println("Destroying....");
    }

}
