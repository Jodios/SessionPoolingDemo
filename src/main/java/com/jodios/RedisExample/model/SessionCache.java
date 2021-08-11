package com.jodios.RedisExample.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class SessionCache implements Serializable {

    private long lastUsed;
    private String token;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (! (o instanceof SessionCache) ) return false;
        return ((SessionCache)o).getToken().equals(this.getToken());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (token == null ? 0 : token.hashCode());
        return result;
    }

}
