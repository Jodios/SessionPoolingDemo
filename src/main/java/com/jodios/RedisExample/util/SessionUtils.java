package com.jodios.RedisExample.util;

import com.jodios.RedisExample.config.Config;
import com.jodios.RedisExample.model.SessionCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Log4j2
@Component
public class SessionUtils {

    Config config;

    @Autowired
    public SessionUtils(Config config){
        this.config = config;
    }

    public boolean sessionHasBeenIdleForTooLong(SessionCache sessionCache){
        long maxIdleTime = config.getMinutes() * 60L * 1000L;
        long idleTime = Instant.now().getEpochSecond() - sessionCache.getLastUsed();
        log.info("Session idle for {}s ({}min)", idleTime, idleTime/60);
        return idleTime > maxIdleTime;
    }

}
