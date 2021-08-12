package com.jodios.RedisExample.service;

public interface ObjectPoolFactory<T> {

    T makeObject() throws Exception;

    void destroyObject(T var1) throws Exception;

    boolean verifyObject(T var1);

}
