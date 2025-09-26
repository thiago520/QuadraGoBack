package com.quadrago.backend.services;

import com.quadrago.backend.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;         // texto (ou base64) da chave

    @Value("${jwt.expiration}")
    private long jwtExpirationMillis; // em milissegundos

    private SecretKey key;

    /* ---------------------- init ---------------------- */

    private SecretKey buildKey(String secret) {
        // Tenta interpretar como Base64; se falhar, usa bytes do texto
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length > 0) {
                log.info("JWT secret loaded as Base64 ({} bytes).", decoded.length);
                return Keys.hmacShaKeyFor(decoded);
            }
        } catch (Exception ignored) { /* not base64, fallback to raw text */ }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // HS256 requer 256 bits (32 bytes) para segurança adequada
            log.warn("JWT secret is short ({} bytes). Consider a 256-bit (32+ bytes) secret or Base64 value.", bytes.length);
        } else {
            log.info("JWT secret loaded as raw text ({} bytes).", bytes.length);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    private SecretKey key() {
        if (key == null) {
            key = buildKey(jwtSecret);
        }
        return key;
    }

    /* -------------------- generation -------------------- */

    public String generateToken(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return buildToken(user.getEmail(), claims, jwtExpirationMillis);
    }

    public String generateToken(UserDetails userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)        // e.g. ROLE_ADMIN
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return buildToken(userDetails.getUsername(), claims, jwtExpirationMillis);
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlMillis) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* -------------------- parsing -------------------- */

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Returns roles claim as a Set<String>. Accepts either List or comma-separated String (for compat). */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object raw = claims.get("roles");
        if (raw == null) return Set.of();
        if (raw instanceof Collection<?> col) {
            return col.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        // fallback: "ADMIN,TEACHER"
        return Arrays.stream(String.valueOf(raw).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // assinatura inválida, malformado, expirado (capturado em isTokenExpired), etc.
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
        // Lança JwtException para tokens inválidos
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
