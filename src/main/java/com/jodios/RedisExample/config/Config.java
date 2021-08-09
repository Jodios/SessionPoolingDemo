package com.jodios.RedisExample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("config")
public class Config {

    private int minutes;
    private int maxSessions;
    private int maxQueueWaitingTime;

}
