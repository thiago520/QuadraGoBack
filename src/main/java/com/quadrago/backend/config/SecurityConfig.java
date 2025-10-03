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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
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

    // Spring interpreta hasRole("ADMIN") como "ROLE_ADMIN"
    private static final String ADMIN   = "ADMIN";
    private static final String TEACHER = "TEACHER";
    private static final String STUDENT = "STUDENT";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Building SecurityFilterChain (stateless, JWT + Basic for Admin endpoints).");

        return http
                // API stateless + CSRF off
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Tratamento de exceções com logs
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loggingAuthEntryPoint())
                        .accessDeniedHandler(loggingAccessDeniedHandler())
                )

                // Autorização
                .authorizeHttpRequests(auth -> auth
                        // --- PÚBLICO ---
                        .requestMatchers("/", "/login", "/logout", "/auth/**", "/assets/**").permitAll()
                        // Exponha com cuidado em produção
                        .requestMatchers("/actuator/**").permitAll()

                        // >>> PÚBLICO: criação de professor <<<
                        // importante: matcher específico ANTES dos matchers protegidos
                        .requestMatchers(HttpMethod.POST, "/teachers").permitAll()
                        // se houver context-path (ex.: /api), acrescente também:
                        // .requestMatchers(HttpMethod.POST, "/api/teachers").permitAll()

                        // PÚBLICO: listagem de planos de um professor
                        .requestMatchers(HttpMethod.GET, "/teachers/*/plans/**").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/api/teachers/*/plans/**").permitAll() // com context-path

                        // --- ÁREAS QUE EXIGEM AUTH (mantém boot admin compat) ---
                        .requestMatchers("/instances/**", "/applications/**").authenticated()

                        // --- ADMIN ---
                        .requestMatchers("/admin/**").hasRole(ADMIN)

                        // --- DOMÍNIO TEACHER (demais rotas protegidas) ---
                        .requestMatchers("/teachers/**").hasAnyRole(TEACHER, ADMIN)
                        // .requestMatchers("/api/teachers/**").hasAnyRole(TEACHER, ADMIN) // com context-path

                        // --- OUTRAS ÁREAS ---
                        .requestMatchers("/class-groups/**").hasAnyRole(TEACHER, ADMIN)
                        .requestMatchers("/traits/**").hasRole(TEACHER)
                        .requestMatchers(HttpMethod.DELETE, "/trait-evaluations/**").hasRole(TEACHER)
                        .requestMatchers("/trait-evaluations/**").hasAnyRole(TEACHER, STUDENT)
                        .requestMatchers("/students/**").hasAnyRole(TEACHER, ADMIN, STUDENT)

                        // Qualquer outra rota requer autenticação
                        .anyRequest().authenticated()
                )

                // Basic Authentication (para Admin/actuator, etc.)
                .httpBasic(Customizer.withDefaults())

                // Filtro JWT (antes do UsernamePasswordAuthenticationFilter)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /** Logs 401 com path e mensagem */
    @Bean
    public AuthenticationEntryPoint loggingAuthEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) -> {
            log.warn("401 Unauthorized - path='{}', message='{}'", request.getRequestURI(), authException.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        };
    }

    /** Logs 403 com path e usuário (se houver) */
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
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
