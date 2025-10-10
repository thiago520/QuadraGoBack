package com.quadrago.backend.filters;

import com.quadrago.backend.services.JwtService;
import com.quadrago.backend.services.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final AntPathMatcher PATH = new AntPathMatcher();

    /** Helper para casar método + padrão (sem context-path; usamos getServletPath()). */
    private boolean matches(HttpServletRequest req, String httpMethod, String pattern) {
        return (httpMethod == null || httpMethod.equalsIgnoreCase(req.getMethod()))
                && PATH.match(pattern, req.getServletPath());
    }

    /**
     * Use getServletPath() para casar caminhos SEM o context-path (ex.: /api).
     * Assim, mesmo que haja server.servlet.context-path, os matchers continuam válidos.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Endpoints sempre públicos
        if (matches(request, null, "/")
                || matches(request, null, "/auth/**")
                || matches(request, null, "/assets/**")
                || matches(request, null, "/login")
                || matches(request, null, "/logout")) {
            return true;
        }

        // Spring Boot Admin (cliente)
        if (matches(request, null, "/instances/**")
                || matches(request, null, "/applications/**")) {
            return true;
        }

        // Actuator: apenas health/info são públicos
        if (matches(request, null, "/actuator/health/**")
                || matches(request, null, "/actuator/info/**")) {
            return true;
        }

        // Cadastro PF (público)
        if (matches(request, "POST", "/users/person")) {
            return true;
        }

        // Catálogo de professores (público)
        if (matches(request, "GET", "/profiles/teacher/**")) {
            return true;
        }

        // Demais rotas: não pular (deixa o filtro rodar)
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Preflight CORS passa direto
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Sem bearer -> segue a chain sem autenticar
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            // 1) Blacklist: se estiver, não autentica
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token na blacklist - path='{}'", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 2) Evita usar refresh token como access (exceto /auth/**, já pulado no shouldNotFilter)
            if (jwtService.isRefreshToken(token)) {
                log.warn("Refresh token apresentado em endpoint não autorizado para refresh. path='{}'", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 3) Fluxo normal de autenticação
            final String username = jwtService.extractUsername(token);
            final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && (currentAuth == null || !username.equalsIgnoreCase(getPrincipalName(currentAuth)))) {
                final var userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    final var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authenticated: user='{}', path='{}'", username, request.getRequestURI());
                } else {
                    log.warn("Invalid JWT token for user='{}', path='{}'", username, request.getRequestURI());
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Expired JWT: path='{}', msg='{}'", request.getRequestURI(), e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) {
            log.warn("Invalid JWT signature: path='{}', msg='{}'", request.getRequestURI(), e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed JWT: path='{}', msg='{}'", request.getRequestURI(), e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("Unsupported JWT: path='{}', msg='{}'", request.getRequestURI(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT processing error: path='{}', msg='{}'", request.getRequestURI(), e.getMessage());
        } catch (Exception e) {
            // catch-all para nunca quebrar a chain em rotas permitAll()
            log.error("Unexpected error while processing JWT: path='{}'", request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getPrincipalName(Authentication auth) {
        if (auth == null) return null;
        final Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails u) {
            return u.getUsername();
        }
        return String.valueOf(principal);
    }
}
