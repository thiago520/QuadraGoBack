package com.quadrago.backend.services;

import com.quadrago.backend.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;  // texto (ou Base64) da chave

    // Tempos padrão (15m access, 30d refresh) — pode ajustar no application.properties
    @Value("${jwt.access.expiration:900000}")
    private long accessTtlMs;

    @Value("${jwt.refresh.expiration:2592000000}")
    private long refreshTtlMs;

    private SecretKey key;

    /* ---------------------- secret/key ---------------------- */

    private SecretKey buildKey(String secret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length > 0) {
                log.info("JWT secret loaded as Base64 ({} bytes).", decoded.length);
                return Keys.hmacShaKeyFor(decoded);
            }
        } catch (Exception ignored) { /* not base64, fallback to raw text */ }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            log.warn("JWT secret is short ({} bytes). Use 256-bit (>=32 bytes) or Base64.", bytes.length);
        } else {
            log.info("JWT secret loaded as raw text ({} bytes).", bytes.length);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    private SecretKey key() {
        if (key == null) key = buildKey(jwtSecret);
        return key;
    }

    /* -------------------- generation -------------------- */

    /** ACCESS TOKEN (curta duração) */
    public String generateAccessToken(UserDetails user) {
        Map<String, Object> claims = baseClaims(user);
        return buildToken(user.getUsername(), claims, accessTtlMs, "access");
    }

    /** REFRESH TOKEN (longa duração) */
    public String generateRefreshToken(UserDetails user) {
        Map<String, Object> claims = baseClaims(user); // pode ser vazio se preferir
        return buildToken(user.getUsername(), claims, refreshTtlMs, "refresh");
    }

    /** Compatibilidade: usa access token por padrão. */
    public String generateToken(UserDetails user) {
        return generateAccessToken(user);
    }

    /** Compatibilidade: usa access token a partir da entidade User. */
    public String generateToken(User user) {
        String username = user.getEmail().toLowerCase(Locale.ROOT);
        Set<String> roles = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName().name())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return buildToken(username, claims, accessTtlMs, "access");
    }

    private Map<String, Object> baseClaims(UserDetails user) {
        Set<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // ROLE_ADMIN, ROLE_TEACHER...
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return claims;
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlMillis, String typ) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMillis);

        // “typ” indica se é access ou refresh — usamos em isRefreshToken()
        Map<String, Object> withTyp = new HashMap<>(claims == null ? Map.of() : claims);
        withTyp.put("typ", typ);

        return Jwts.builder()
                .setClaims(withTyp)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* -------------------- parsing/validation -------------------- */

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /** Retorna roles como Set<String> (mesmo formato gravado no claim, ex.: ROLE_ADMIN). */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Object raw = extractAllClaims(token).get("roles");
        if (raw == null) return Set.of();
        if (raw instanceof Collection<?> col) {
            return col.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Arrays.stream(String.valueOf(raw).split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** true se o claim "typ" == "refresh" */
    public boolean isRefreshToken(String token) {
        Object typ = extractAllClaims(token).get("typ");
        return typ != null && "refresh".equalsIgnoreCase(String.valueOf(typ));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username != null
                    && username.equalsIgnoreCase(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
