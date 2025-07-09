package com.quadrago.backend.controllers;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.User;
import com.quadrago.backend.services.AuthService;
import com.quadrago.backend.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        String role = body.get("role");

        User user = authService.register(name, email, password, RoleName.valueOf(role));
        String token = jwtService.generateToken(user);

        return Map.of("token", token, "user", user.getEmail(), "roles", role);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = authService.authenticate(email, password);
        String token = jwtService.generateToken(user);

        return Map.of("token", token, "user", user.getEmail(), "roles", user.getRoles());
    }
}
