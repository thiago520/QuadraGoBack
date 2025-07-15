package com.quadrago.backend.config;

import com.quadrago.backend.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir acesso sem autenticação para rotas públicas e actuator
                        .requestMatchers(
                                "/auth/**", "/", "/assets/**", "/login", "/logout", "/actuator/**"
                        ).permitAll()
                        // Rotas do Spring Boot Admin que precisam de autenticação básica
                        // A ordem é importante: coloque-as antes de outras regras mais genéricas
                        .requestMatchers("/instances", "/instances/**", "/applications", "/applications/**").authenticated()
                        // Rotas específicas de role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/professor/**").hasAnyRole("PROFESSOR", "ADMIN")
                        // Todas as outras requisições precisam de autenticação
                        .anyRequest().authenticated()
                )
                // Habilitar Basic Authentication para o Spring Boot Admin
                .httpBasic(Customizer.withDefaults())
                // Adicionar o filtro JWT antes do filtro de autenticação de usuário/senha
                // Este filtro será ignorado para as rotas do shouldNotFilter do JwtAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
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