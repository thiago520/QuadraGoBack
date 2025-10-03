package com.quadrago.backend.filters;

import com.quadrago.backend.services.CustomUserDetailsService;
import com.quadrago.backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Use getServletPath() para casar caminhos SEM o context-path (ex.: /api).
     * Assim, mesmo que haja server.servlet.context-path, os matchers continuam válidos.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String servletPath = request.getServletPath(); // path dentro do contexto
        final String method = request.getMethod();

        // Endpoints públicos e compatibilidade com Spring Boot Admin
        if (servletPath.startsWith("/auth")
                || servletPath.startsWith("/actuator")
                || "/".equals(servletPath)
                || servletPath.startsWith("/assets")
                || servletPath.startsWith("/login")
                || servletPath.startsWith("/logout")
                || servletPath.startsWith("/instances")
                || servletPath.startsWith("/applications")) {
            return true;
        }

        // >>> PÚBLICO: criação de professor (POST /teachers)
        if ("POST".equalsIgnoreCase(method) && "/teachers".equals(servletPath)) {
            return true;
        }
        // Se sua app expõe como /api/teachers (via context-path + controller),
        // não precisa tratar aqui pois getServletPath() já remove o context-path.
        // Mas, se houver prefixos adicionais no Controller, adicione os matches equivalentes.

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
                    // Token inválido -> apenas loga e segue SEM autenticar (não lança exceção!)
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
