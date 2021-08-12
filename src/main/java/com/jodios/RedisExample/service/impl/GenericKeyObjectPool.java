package com.jodios.RedisExample.service.impl;

import com.jodios.RedisExample.service.KeyObjectPool;
import com.jodios.RedisExample.service.ObjectPoolFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class GenericKeyObjectPool<K, V> implements KeyObjectPool<K, V> {

    private final Map<K, BlockingDeque<V>> queueMap;
    private final Map<K, AtomicInteger> createdObjectCountMap;
    private final ObjectPoolFactory<V> factory;
    private final int maxSize;
    private final int waitTimeInMilliSeconds;

    public GenericKeyObjectPool(ObjectPoolFactory<V> factory, int maxQueueSize, int waitTimeInMilliSeconds){
        queueMap = new ConcurrentHashMap<>();
        createdObjectCountMap = new ConcurrentHashMap<>();
        this.factory = factory;
        this.maxSize = maxQueueSize;
        this.waitTimeInMilliSeconds = waitTimeInMilliSeconds;
    }

    @Override
    public void returnToPool(K k, V v) throws Exception {
        if(queueMap.containsKey(k)){
            if( !queueMap.get(k).offerFirst(v, this.waitTimeInMilliSeconds, TimeUnit.MILLISECONDS) ){
                factory.destroyObject(v);
            }
        }else{
            BlockingDeque<V> newQueue = initializeQueue(k);
            createdObjectCountMap.get(k).incrementAndGet();
            newQueue.putFirst(v);
        }
    }

    @Override
    public V getFromPool(K k) throws Exception {

        if (queueMap.containsKey(k)) {
            log.info("Getting object from {} and QUEUE@{}", k.toString(), Integer.toHexString(queueMap.get(k).hashCode()));
            BlockingDeque<V> queue = queueMap.get(k);
            if (queue.isEmpty() && createdObjectCountMap.get(k).get() >= this.maxSize) {
                V v = queue.pollFirst(this.waitTimeInMilliSeconds, TimeUnit.MILLISECONDS);
                if (v == null) {
                    return factory.makeObject();
                }
                return v;
            }

            V v;
            if (queue.isEmpty()) {
                v = factory.makeObject();
                if (createdObjectCountMap.get(k).get() < this.maxSize)
                    createdObjectCountMap.get(k).incrementAndGet();
            } else {
                v = queue.takeFirst();
                while (factory.verifyObject(v)) {
                    factory.destroyObject(v);
                    createdObjectCountMap.get(k).decrementAndGet();
                    if (queue.isEmpty()) {
                        if (createdObjectCountMap.get(k).get() < this.maxSize)
                            createdObjectCountMap.get(k).incrementAndGet();
                        return factory.makeObject();
                    }
                    v = queue.takeFirst();
                }
            }
            return v;
        }

        BlockingDeque<V> newQueue = initializeQueue(k);
        if (createdObjectCountMap.get(k).get() < this.maxSize)
            createdObjectCountMap.get(k).incrementAndGet();
        return factory.makeObject();
    }

    @Override
    public void destroy() throws Exception {
        log.info("Destroying...");
        //TODO: do something when service shuts down
    }

    private BlockingDeque<V> initializeQueue(final K k){
        log.info("Initializing queue for {}", k.toString());
        if(queueMap.containsKey(k)) return queueMap.get(k);
        BlockingDeque<V> newQueue = new LinkedBlockingDeque<>(this.maxSize);
        queueMap.put(k, newQueue);
        createdObjectCountMap.put(k, new AtomicInteger(0));
        return newQueue;
    }


}
