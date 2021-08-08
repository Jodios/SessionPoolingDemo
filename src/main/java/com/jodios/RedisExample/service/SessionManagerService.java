package com.jodios.RedisExample.service;

import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.util.SessionUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
public class SessionManagerService {

    private final String SESSION_QUEUE = "QUEUE";
    private final Queue<SessionCache> sessionQueue = new ArrayDeque<>();

    SabreSessionService sessionService;
    SessionUtils utils;

    @Autowired
    public SessionManagerService(SessionUtils utils, SabreSessionService sessionService){
        this.utils = utils;
        this.sessionService = sessionService;
    }

    public void save(final String epr, SessionCache session){
        log.info("Adding session to {}", epr);
        sessionQueue.add(session);
        hashOperations.put(SESSION_QUEUE, epr, sessionQueue);
    }

    public SessionCache getEPRSession(final String epr) throws Exception {
        log.info("Getting session from {}", epr);
        Queue<SessionCache> sessionQueue = hashOperations.get(SESSION_QUEUE, epr);
        if(sessionQueue != null && !sessionQueue.isEmpty() && utils.sessionHasBeenIdleForTooLong(sessionQueue.peek()) ){
            sessionQueue.clear();
        }
        SessionCache session = sessionQueue.isEmpty() ? sessionService.getNewSession() : sessionQueue.remove();
        hashOperations.put(SESSION_QUEUE, epr, sessionQueue);
        return session;
    }

    public Queue<SessionCache> findByEPR(final String epr){
        return hashOperations.get(SESSION_QUEUE, epr);
    }

    public Map<String, Queue<SessionCache>> findAll(){
        return hashOperations.entries(SESSION_QUEUE);
    }

    public void delete(String epr){
        hashOperations.delete(SESSION_QUEUE, epr);
    }

}
