package com.jodios.RedisExample.service;

import com.jodios.RedisExample.config.Config;
import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.util.SessionUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class SessionPoolService implements DisposableBean {

    private final Map<String, BlockingDeque<SessionCache>> eprToQueue;
    private final Map<String, AtomicInteger> openSessionsMap;
    private final SabreSessionService sessionService;
    private final SessionUtils utils;
    private final Config config;

    @Autowired
    public SessionPoolService(SessionUtils utils, SabreSessionService sessionService, Config config){
        this.utils = utils;
        this.config = config;
        this.sessionService = sessionService;
        this.openSessionsMap = new ConcurrentHashMap<>();
        this.eprToQueue = new ConcurrentHashMap<>();
    }

    public void addToPool(final String epr, SessionCache session) throws InterruptedException {
        log.info("Adding session to {}", epr);
        if(eprToQueue.containsKey(epr)){
            if( eprToQueue.get(epr).offerFirst(session, 1, TimeUnit.SECONDS) ){
                // TODO if timeout then close session
            }
        }else{
            BlockingDeque<SessionCache> newQueue = initializeQueue(epr);
            openSessionsMap.get(epr).incrementAndGet();
            newQueue.putFirst(session);
        }
    }

    public SessionCache getFromPool(final String epr) throws Exception {
        log.info("Getting session from {} and QUEUE@{}", epr, Integer.toHexString(eprToQueue.get(epr).hashCode()));

        if(eprToQueue.containsKey(epr)){
            BlockingDeque<SessionCache> sessionQueue = eprToQueue.get(epr);
            if(sessionQueue.isEmpty() && openSessionsMap.get(epr).get()>=config.getMaxSessions()){
                log.info("=========================WAITING FOR {} SECONDS===============================", config.getMaxQueueWaitingTime());
                SessionCache session =  sessionQueue.pollFirst(config.getMaxQueueWaitingTime(), TimeUnit.SECONDS);
                if(session == null){
                    return sessionService.getNewSession();
                }
            }
            // [ 4, 3, 2, 1 ]
            SessionCache session;
            if(sessionQueue.isEmpty()){
                session = sessionService.getNewSession();
                openSessionsMap.get(epr).incrementAndGet();
            }else{
                session = sessionQueue.takeFirst();
                while(utils.sessionHasBeenIdleForTooLong(session)){
                    openSessionsMap.get(epr).decrementAndGet();
                    if(sessionQueue.isEmpty()){
                        openSessionsMap.get(epr).incrementAndGet();
                        return sessionService.getNewSession();
                    }
                    session = sessionQueue.takeFirst();
                }
            }
            return session;
        }

        BlockingDeque<SessionCache> newQueue = new LinkedBlockingDeque<>(config.getMaxSessions());
        eprToQueue.put(epr, newQueue);
        openSessionsMap.put(epr, new AtomicInteger(1));
        return sessionService.getNewSession();
    }

    public BlockingDeque<SessionCache> initializeQueue(final String epr){
        log.info("===========INITIALIZED QUEUE FOR {}===========", epr);
        if(eprToQueue.containsKey(epr)) return eprToQueue.get(epr);
        BlockingDeque<SessionCache> newQueue = new LinkedBlockingDeque<>(config.getMaxSessions());
        eprToQueue.put(epr, newQueue);
        openSessionsMap.put(epr, new AtomicInteger(0));
        return newQueue;
    }

    public BlockingDeque<SessionCache> findByEPR(final String epr){
        return eprToQueue.get(epr);
    }

    public Map<String, BlockingDeque<SessionCache>> findAll(){
        return eprToQueue;
    }

    public void delete(String epr){
        openSessionsMap.remove(epr);
        eprToQueue.remove(epr);
    }

    @Scheduled( fixedDelayString = "${refreshScheduleDelayInMilliseconds}" )
    public void refreshOldTokens(){
        log.info("REMOVING OLD TOKENS FROM QUEUE");
        eprToQueue.keySet().forEach(k -> {
            eprToQueue.get(k).forEach(session -> {
                if(utils.sessionHasBeenIdleForTooLong(session)){
                    eprToQueue.get(k).remove(session);
                    openSessionsMap.get(k).decrementAndGet();
                }
            });
        });
    }

    @Override
    public void destroy() throws Exception {
        // TODO: When session is closed then close all active sessions
        log.info("Destroying....");
    }

}
