package com.quadrago.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /** Coloca o token na blacklist até o instante de expiração (epoch millis). */
    public void blacklist(String token, long expiresAtMillis) {
        if (token == null) return;
        blacklist.put(token, expiresAtMillis);
    }

    /** Verifica se o token está blacklisted; limpa entradas expiradas on-the-fly. */
    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Long exp = blacklist.get(token);
        if (exp == null) return false;
        long now = System.currentTimeMillis();
        if (exp <= now) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /** Opcional: limpeza manual (pode agendar com @Scheduled, se quiser). */
    public void cleanup() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = blacklist.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() <= now) it.remove();
        }
    }
}
