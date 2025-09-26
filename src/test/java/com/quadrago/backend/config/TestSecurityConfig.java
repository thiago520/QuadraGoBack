package com.quadrago.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod; // <—
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
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        // >>> Regras específicas do recurso:
                        .requestMatchers(HttpMethod.POST, "/trait-evaluations/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT,  "/trait-evaluations/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE,"/trait-evaluations/**").hasRole("TEACHER")
                        // leitura pode ser mais aberta; ajuste se precisar:
                        .requestMatchers(HttpMethod.GET, "/trait-evaluations/**").hasAnyRole("TEACHER","ADMIN")
                        // demais endpoints exigem estar autenticado
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
