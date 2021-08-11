package com.jodios.RedisExample.service;

import com.jodios.RedisExample.config.Config;
import com.jodios.RedisExample.factory.SessionObjectFactory;
import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.util.SessionUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class SessionPoolService implements DisposableBean {

    private final Map<String, BlockingDeque<SessionCache>> eprToQueue;
    private final Map<String, ObjectPool<SessionCache>> poolMap;
    private final Map<String, AtomicInteger> openSessionsMap;
    PooledObjectFactory<SessionCache> factory;
    private final SabreSessionService sessionService;
    private final SessionUtils utils;
    private final Config config;

    @Autowired
    public SessionPoolService(SessionUtils utils, SabreSessionService sessionService, Config config, PooledObjectFactory<SessionCache> factory){
        this.utils = utils;
        this.config = config;
        this.factory = factory;
        this.sessionService = sessionService;
        this.openSessionsMap = new ConcurrentHashMap<>();
        this.eprToQueue = new ConcurrentHashMap<>();
        this.poolMap = new ConcurrentHashMap<>();
    }

    public void addToPool(final String epr, SessionCache session) throws Exception {
        log.info("Adding session to {}", epr);
//        if(eprToQueue.containsKey(epr)){
//            if( eprToQueue.get(epr).offerFirst(session, 1, TimeUnit.SECONDS) ){
//                // TODO if timeout then close session
//            }
//        }else{
//            BlockingDeque<SessionCache> newQueue = initializeQueue(epr);
//            openSessionsMap.get(epr).incrementAndGet();
//            newQueue.putFirst(session);
//        }
        if(poolMap.containsKey(epr)){
            ObjectPool<SessionCache> pool = poolMap.get(epr);
            // Will call the Destroy method in factory if it's full
            pool.returnObject(session);
        }else{
            ObjectPool<SessionCache> newPool = initializePool(epr);
            newPool.returnObject(session);
        }
    }

    public SessionCache getFromPool(final String epr) throws Exception {
        log.info("Getting session from {} and QUEUE@{}", epr, Integer.toHexString(eprToQueue.get(epr).hashCode()));
        if(poolMap.containsKey(epr)){
            try{
                ObjectPool<SessionCache> pool = poolMap.get(epr);
                SessionCache session = pool.borrowObject();
                return session;
            }catch (NoSuchElementException ex){
                return sessionService.getNewSession();
            }
        }
        ObjectPool<SessionCache> pool = initializePool(epr);
        poolMap.put(epr, pool);
        return pool.borrowObject();
    }

    public BlockingDeque<SessionCache> initializeQueue(final String epr){
        log.info("===========INITIALIZED QUEUE FOR {}===========", epr);
        if(eprToQueue.containsKey(epr)) return eprToQueue.get(epr);
        BlockingDeque<SessionCache> newQueue = new LinkedBlockingDeque<>(config.getMaxSessions());
        eprToQueue.put(epr, newQueue);
        openSessionsMap.put(epr, new AtomicInteger(0));
        return newQueue;
    }

    public ObjectPool<SessionCache> initializePool(final String epr){
        log.info("===========INITIALIZED POOL FOR {}===========", epr);
        if(poolMap.containsKey(epr)) return poolMap.get(epr);
        GenericObjectPoolConfig<SessionCache> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(0);
        poolConfig.setMaxTotal(config.getMaxSessions());
        poolConfig.setMaxWaitMillis(config.getMaxQueueWaitingTime());
        GenericObjectPool<SessionCache> newPool = new GenericObjectPool<>(factory, poolConfig);
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setRemoveAbandonedOnBorrow(false);
        EvictionConfig evictionConfig = new EvictionConfig(500000, 50000, 500000);
        DefaultEvictionPolicy<SessionCache> evictionPolicy = new DefaultEvictionPolicy<>();
        newPool.setEvictionPolicy(evictionPolicy);
        newPool.setAbandonedConfig(abandonedConfig);
        poolMap.put(epr, newPool);
        openSessionsMap.put(epr, new AtomicInteger(0));
        return newPool;
    }

    public BlockingDeque<SessionCache> findByEPR(final String epr){
        return eprToQueue.get(epr);
    }

    public Map<String, ObjectPool<SessionCache>> findAll(){
        return poolMap;
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
