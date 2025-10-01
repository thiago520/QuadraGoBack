package com.quadrago.backend.config;

import com.quadrago.backend.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Role constants (Spring expects authorities like ROLE_ADMIN; hasRole("ADMIN") checks for that)
    private static final String ADMIN   = "ADMIN";
    private static final String TEACHER = "TEACHER";
    private static final String STUDENT = "STUDENT";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Building SecurityFilterChain (stateless, JWT + Basic for Admin endpoints).");

        return http
                // Stateless API + CSRF off
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Exception handling with logging
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loggingAuthEntryPoint())
                        .accessDeniedHandler(loggingAccessDeniedHandler())
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ---
                        .requestMatchers("/", "/login", "/logout", "/auth/**", "/assets/**").permitAll()
                        // actuator (exponha com cuidado em prod)
                        .requestMatchers("/actuator/**").permitAll()

                        // >>> TEACHER SIGN-UP (público) <<<
                        .requestMatchers(HttpMethod.POST, "/teachers").permitAll()

                        // --- SPRING BOOT ADMIN (requires auth; Basic enabled below) ---
                        .requestMatchers("/instances/**", "/applications/**").authenticated()

                        // --- ADMIN ---
                        .requestMatchers("/admin/**").hasRole(ADMIN)

                        // --- TEACHER DOMAIN (demais rotas protegidas) ---
                        .requestMatchers("/teachers/**").hasAnyRole(TEACHER, ADMIN)
                        .requestMatchers("/class-groups/**").hasAnyRole(TEACHER, ADMIN)
                        .requestMatchers("/traits/**").hasRole(TEACHER)

                        // --- TRAIT EVALUATIONS ---
                        .requestMatchers(HttpMethod.DELETE, "/trait-evaluations/**").hasRole(TEACHER)
                        .requestMatchers("/trait-evaluations/**").hasAnyRole(TEACHER, STUDENT)

                        // --- STUDENTS ---
                        .requestMatchers("/students/**").hasAnyRole(TEACHER, ADMIN, STUDENT)

                        // Any other endpoint must be authenticated
                        .anyRequest().authenticated()
                )

                // Basic Authentication (kept for Spring Boot Admin compatibility)
                .httpBasic(Customizer.withDefaults())

                // JWT filter (runs before UsernamePasswordAuthenticationFilter)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /** Logs 401s with request path and message */
    @Bean
    public AuthenticationEntryPoint loggingAuthEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                log.warn("401 Unauthorized - path='{}', message='{}'", request.getRequestURI(), authException.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }
        };
    }

    /** Logs 403s with request path and user (if any) */
    @Bean
    public AccessDeniedHandler loggingAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String user = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
            log.warn("403 Forbidden - user='{}', path='{}', reason='{}'", user, request.getRequestURI(), accessDeniedException.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // default strength 10; tune if needed
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
