package com.quadrago.backend.dashboard.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}

    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;

        Object p = auth.getPrincipal();
        // Se seu principal tiver getId()
        try {
            var m = p.getClass().getMethod("getId");
            Object id = m.invoke(p);
            if (id instanceof Number n) return n.longValue();
        } catch (Exception ignored) {}

        // fallback: se o nome for numérico
        try {
            return Long.valueOf(auth.getName());
        } catch (Exception ignored) {}

        return null;
    }
}
