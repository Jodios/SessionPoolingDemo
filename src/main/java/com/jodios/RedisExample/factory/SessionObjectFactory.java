package com.jodios.RedisExample.factory;

import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.service.SabreSessionService;
import com.jodios.RedisExample.service.SessionPoolService;
import com.jodios.RedisExample.util.SessionUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SessionObjectFactory implements PooledObjectFactory<SessionCache> {

    @Autowired
    SabreSessionService service;
    @Autowired
    SessionUtils utils;
    @Override
    public PooledObject<SessionCache> makeObject() throws Exception {
        return new DefaultPooledObject<>(service.getNewSession());
    }

    @Override
    public void destroyObject(PooledObject<SessionCache> pooledObject) throws Exception {
        log.info("Destroying Object");
    }

    @Override
    public boolean validateObject(PooledObject<SessionCache> pooledObject) {
        return utils.sessionHasBeenIdleForTooLong(  pooledObject.getObject() );
    }

    @Override
    public void activateObject(PooledObject<SessionCache> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<SessionCache> pooledObject) throws Exception {

    }
}
