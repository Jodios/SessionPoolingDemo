package com.jodios.RedisExample.service.impl;

import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.service.ObjectPoolFactory;
import com.jodios.RedisExample.service.SabreSessionService;
import com.jodios.RedisExample.util.SessionUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SessionObjectPoolFactory implements ObjectPoolFactory<SessionCache> {
    @Autowired
    SessionUtils utils;
    @Autowired
    SabreSessionService service;

    @Override
    public SessionCache makeObject() throws Exception {
        return service.getNewSession();
    }

    @Override
    public void destroyObject(SessionCache session) throws Exception {
        //TODO: CLOSE SESSION
        log.info("Closing Session...");
    }

    @Override
    public boolean verifyObject(SessionCache session) {
        return utils.sessionHasBeenIdleForTooLong(session);
    }
}
