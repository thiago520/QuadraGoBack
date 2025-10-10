package com.quadrago.backend.config;

import com.quadrago.backend.filters.CustomUserDetailsService;
import com.quadrago.backend.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    // Spring interpreta hasRole("ADMIN") como "ROLE_ADMIN"
    private static final String ADMIN = "ADMIN";
    private static final String TEACHER = "TEACHER";
    private static final String STUDENT = "STUDENT";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Building SecurityFilterChain (stateless, JWT).");

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loggingAuthEntryPoint())
                        .accessDeniedHandler(loggingAccessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                        // ======= PÚBLICO =======
                        .requestMatchers("/", "/login", "/logout", "/auth/**", "/assets/**").permitAll()

                        // Actuator: health+info públicos; demais exigem ADMIN
                        .requestMatchers("/actuator/health/**", "/actuator/info/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole(ADMIN)

                        // Cadastro de usuário Pessoa Física
                        .requestMatchers(HttpMethod.POST, "/users/person").permitAll()

                        // Perfil de professor público para consulta
                        .requestMatchers(HttpMethod.GET, "/profiles/teacher/**").permitAll()

                        // Criação de perfis exige autenticação (sem exigir role prévia)
                        .requestMatchers(HttpMethod.POST, "/profiles/*/teacher").authenticated()
                        .requestMatchers(HttpMethod.POST, "/profiles/*/student").authenticated()

                        // Atualizações de TEACHER/STUDENT
                        .requestMatchers(HttpMethod.PUT, "/profiles/teacher/**").hasAnyRole(TEACHER, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/profiles/teacher/**").hasAnyRole(TEACHER, ADMIN)

                        .requestMatchers(HttpMethod.GET, "/profiles/student/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/profiles/student/**").hasAnyRole(STUDENT, ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/profiles/student/**").hasAnyRole(STUDENT, ADMIN)

                        // Área administrativa
                        .requestMatchers("/admin/**").hasRole(ADMIN)

                        // Qualquer outra rota requer autenticação
                        .anyRequest().authenticated()
                )

                // Usa nosso AuthenticationProvider (UserDetailsService + BCrypt)
                .authenticationProvider(authenticationProvider())

                // Basic Auth (útil para admin/actuator quando necessário)
                .httpBasic(Customizer.withDefaults())

                // JWT antes do UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /** DaoAuthenticationProvider usando CustomUserDetailsService + BCrypt */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** CORS simples (ajuste origin/headers/methods conforme seu front) */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("*")); // TROQUE em produção
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
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
