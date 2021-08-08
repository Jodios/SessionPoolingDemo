package com.jodios.RedisExample.controller;

import com.jodios.RedisExample.model.SessionCache;
import com.jodios.RedisExample.model.SessionManagerRequest;
import com.jodios.RedisExample.service.SessionManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Queue;

@RestController
@RequestMapping("/sessionManager")
public class SessionManagerController {

    @Autowired
    SessionManagerService service;

    @PostMapping()
    public Map<String, Queue<SessionCache>> save(@RequestBody final SessionManagerRequest request){
        service.save(request.getEpr(), request.getSession());
        return getAll();
    }
    @GetMapping
    public SessionCache getSession(@RequestParam String epr) throws Exception {
        return service.getEPRSession(epr);
    }
    @GetMapping("/getAll")
    public Map<String, Queue<SessionCache>> getAll(){
        return service.findAll();
    }
    @DeleteMapping("/deleteAll")
    public Map<String, Queue<SessionCache>> deleteAll(){
        getAll().keySet().forEach(epr -> service.delete(epr));
        return getAll();
    }



}
