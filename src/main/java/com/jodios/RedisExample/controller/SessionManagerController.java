package com.jodios.RedisExample.controller;

import com.jodios.RedisExample.config.Config;
import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.model.SessionManagerRequest;
import com.jodios.RedisExample.service.KeyObjectPool;
import com.jodios.RedisExample.service.impl.GenericKeyObjectPool;
import com.jodios.RedisExample.service.impl.SessionObjectPoolFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/sessionManager")
public class SessionManagerController {

    @Autowired
    Config config;
    @Autowired
    SessionObjectPoolFactory factory;

    KeyObjectPool<String, SessionCache> service;

    @PostConstruct
    public void init(){
        service = new GenericKeyObjectPool<>(factory, config.getMaxSessions(), config.getMaxQueueWaitingTime());
    }

    @PostMapping()
    public String save(@RequestBody final SessionManagerRequest request) throws Exception {
        service.returnToPool(request.getEpr(), request.getSession());
        return "DONE";
    }

    @GetMapping
    public SessionCache getSession(@RequestParam String epr) throws Exception {
        return service.getFromPool(epr);
    }

}
