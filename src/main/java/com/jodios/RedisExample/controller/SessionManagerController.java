package com.jodios.RedisExample.controller;

import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.model.SessionManagerRequest;
import com.jodios.RedisExample.service.SessionPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;

@RestController
@RequestMapping("/sessionManager")
public class SessionManagerController {

    @Autowired
    SessionPoolService service;

    @PostMapping()
    public Map<String, BlockingDeque<SessionCache>> save(@RequestBody final SessionManagerRequest request) throws InterruptedException {
        service.addToPool(request.getEpr(), request.getSession());
        return getAll();
    }

    @GetMapping
    public SessionCache getSession(@RequestParam String epr) throws Exception {
        return service.getFromPool(epr);
    }

    @GetMapping("/getAll")
    public Map<String, BlockingDeque<SessionCache>> getAll(){
        return service.findAll();
    }

    @DeleteMapping("/deleteAll")
    public Map<String, BlockingDeque<SessionCache>> deleteAll(){
        getAll().keySet().forEach(epr -> service.delete(epr));
        return getAll();
    }



}
