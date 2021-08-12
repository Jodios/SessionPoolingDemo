package com.jodios.RedisExample.service;

import org.springframework.beans.factory.DisposableBean;

public interface KeyObjectPool<K, V> extends DisposableBean {

    void returnToPool(K k, V v) throws Exception;

    V getFromPool(K k) throws Exception;

}
