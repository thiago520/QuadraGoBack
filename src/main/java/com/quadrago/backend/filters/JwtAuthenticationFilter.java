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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Public & admin-compatibility endpoints
        return path.startsWith("/auth")
                || path.startsWith("/actuator")
                || path.equals("/")
                || path.startsWith("/assets")
                || path.startsWith("/login")
                || path.startsWith("/logout")
                || path.startsWith("/instances")
                || path.startsWith("/applications");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Short-circuit for CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No bearer token -> continue
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            final String username = jwtService.extractUsername(token);
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && (currentAuth == null || !username.equalsIgnoreCase(getPrincipalName(currentAuth)))) {
                var userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
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
            // catch-all to avoid breaking the chain on unexpected issues
            log.error("Unexpected error while processing JWT: path='{}'", request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getPrincipalName(Authentication auth) {
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails u) {
            return u.getUsername();
        }
        return String.valueOf(principal);
    }
}
