package com.quadrago.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- Públicos (inclui o cadastro de professor) ---
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/teachers").permitAll()

                        // --- Protegidos (espelha o comportamento do app em prod) ---
                        .requestMatchers("/teachers/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/class-groups/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/traits/**").hasRole("TEACHER")

                        // Trait-evaluations (como já estava no seu teste)
                        .requestMatchers(HttpMethod.POST,   "/trait-evaluations/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT,    "/trait-evaluations/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/trait-evaluations/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET,    "/trait-evaluations/**").hasAnyRole("TEACHER","ADMIN")

                        // Qualquer outro endpoint exige autenticação
                        .anyRequest().authenticated()
                )
                // httpBasic para suportar @WithMockUser nos testes
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
