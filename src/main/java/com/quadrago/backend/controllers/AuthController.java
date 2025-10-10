package com.quadrago.backend.controllers;

import com.quadrago.backend.filters.CustomUserPrincipal;
import com.quadrago.backend.filters.CustomUserDetailsService;
import com.quadrago.backend.services.JwtService;
import com.quadrago.backend.services.TokenBlacklistService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService blacklist;

    /* ======== DTOs ======== */
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record TokenPairResponse(String accessToken, String refreshToken, String tokenType,
                                    Long userId, String email, Set<String> roles) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(String refreshToken) {}

    /* ======== LOGIN ======== */
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@RequestBody @Validated LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
            );
            CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();

            String access = jwtService.generateAccessToken(principal);
            String refresh = jwtService.generateRefreshToken(principal);

            Set<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).map(a -> a.replaceFirst("^ROLE_", ""))
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(new TokenPairResponse(access, refresh, "Bearer",
                    principal.getId(), principal.getUsername(), roles));

        } catch (BadCredentialsException e) {
            log.warn("Login inválido: {}", request.email());
            return ResponseEntity.status(401).build();
        } catch (DisabledException e) {
            log.warn("Conta desabilitada: {}", request.email());
            return ResponseEntity.status(403).build();
        } catch (LockedException e) {
            log.warn("Conta bloqueada: {}", request.email());
            return ResponseEntity.status(423).build();
        }
    }

    /* ======== REFRESH (rotação de refresh) ======== */
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody @Validated RefreshRequest req) {
        final String oldRefresh = req.refreshToken();

        // 1) Blacklist check
        if (blacklist.isBlacklisted(oldRefresh)) {
            log.warn("Refresh token na blacklist");
            return ResponseEntity.status(401).build();
        }

        // 2) Validar se é refresh e se está válido
        final String username = jwtService.extractUsername(oldRefresh);
        if (!jwtService.isRefreshToken(oldRefresh) || username == null) {
            return ResponseEntity.status(401).build();
        }

        var userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(oldRefresh, userDetails)) {
            return ResponseEntity.status(401).build();
        }

        // 3) Rotação: invalida o refresh antigo até seu expiration
        long expMs = jwtService.extractExpiration(oldRefresh).getTime();
        blacklist.blacklist(oldRefresh, expMs);

        // 4) Gera novos tokens
        String newAccess = jwtService.generateAccessToken(userDetails);
        String newRefresh = jwtService.generateRefreshToken(userDetails);

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).map(a -> a.replaceFirst("^ROLE_", ""))
                .collect(Collectors.toSet());

        Long userId = (userDetails instanceof CustomUserPrincipal p) ? p.getId() : null;

        return ResponseEntity.ok(new TokenPairResponse(newAccess, newRefresh, "Bearer",
                userId, userDetails.getUsername(), roles));
    }

    /* ======== LOGOUT (blacklist de access e refresh) ======== */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody(required = false) LogoutRequest body
    ) {
        // Access token no Authorization: Bearer <token>
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String access = authorization.substring(7);
            try {
                long accessExp = jwtService.extractExpiration(access).getTime();
                blacklist.blacklist(access, accessExp);
            } catch (Exception e) {
                log.debug("Falha ao extrair expiração do access token no logout: {}", e.getMessage());
            }
        }

        // Refresh token opcional no corpo
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            String refresh = body.refreshToken();
            try {
                long refreshExp = jwtService.extractExpiration(refresh).getTime();
                blacklist.blacklist(refresh, refreshExp);
            } catch (Exception e) {
                log.debug("Falha ao extrair expiração do refresh token no logout: {}", e.getMessage());
            }
        }

        return ResponseEntity.noContent().build();
    }
}
