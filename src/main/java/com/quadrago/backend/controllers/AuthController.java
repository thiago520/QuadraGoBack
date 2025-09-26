package com.quadrago.backend.controllers;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.User;
import com.quadrago.backend.services.AuthService;
import com.quadrago.backend.services.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest body) {
        RoleName roleName = parseRole(body.getRole());
        if (roleName == null) {
            log.warn("Registro recusado: role inválida='{}' para email='{}'", body.getRole(), body.getEmail());
            return ResponseEntity.badRequest()
                    .body(AuthResponse.error("Role inválida: use ADMIN, TEACHER ou STUDENT."));
        }

        User user = authService.register(body.getName(), body.getEmail(), body.getPassword(), roleName);
        String token = jwtService.generateToken(user);

        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        log.info("Usuário registrado com sucesso: email='{}', roles={}", user.getEmail(), roles);
        return ResponseEntity.ok(AuthResponse.success(token, user.getEmail(), roles));
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        try {
            User user = authService.authenticate(body.getEmail(), body.getPassword());
            String token = jwtService.generateToken(user);

            Set<String> roles = user.getRoles().stream()
                    .map(r -> r.getName().name())
                    .collect(Collectors.toSet());

            log.info("Login efetuado: email='{}', roles={}", user.getEmail(), roles);
            return ResponseEntity.ok(AuthResponse.success(token, user.getEmail(), roles));
        } catch (RuntimeException ex) {
            // Ajuste o tipo de exceção se seu AuthService lançar algo específico
            log.warn("Falha de login: email='{}' - motivo='{}'", body.getEmail(), ex.getMessage());
            return ResponseEntity.status(401).body(AuthResponse.error("Credenciais inválidas."));
        }
    }

    /* ---------- helpers ---------- */

    private RoleName parseRole(String roleRaw) {
        if (roleRaw == null) return null;
        try {
            return RoleName.valueOf(roleRaw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /* ---------- DTOs ---------- */

    @Data
    public static class RegisterRequest {
        @NotBlank private String name;
        @Email @NotBlank private String email;
        @NotBlank private String password;
        @NotBlank private String role; // ADMIN | TEACHER | STUDENT
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private boolean success;
        private String token;
        private String user;
        private Set<String> roles;
        private String error;

        public static AuthResponse success(String token, String user, Set<String> roles) {
            return AuthResponse.builder()
                    .success(true)
                    .token(token)
                    .user(user)
                    .roles(roles)
                    .build();
        }

        public static AuthResponse error(String message) {
            return AuthResponse.builder()
                    .success(false)
                    .error(message)
                    .build();
        }
    }
}
