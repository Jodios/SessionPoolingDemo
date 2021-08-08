package com.jodios.RedisExample.service;

import com.jodios.RedisExample.model.SessionCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class SabreSessionService {

    public SessionCache getNewSession() throws InterruptedException {
        log.info("Creating a new session...");
        TimeUnit.MILLISECONDS.sleep(200);
        SessionCache cache = new SessionCache();
        cache.setToken(java.util.UUID.randomUUID().toString());
        cache.setLastUsed(Instant.now().getEpochSecond());
        return cache;
    }

}
