package com.quadrago.backend.services;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.Role;
import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.RoleRepository;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with a single role.
     * Validates duplicate email (case-insensitive).
     */
    @Transactional
    public User register(String name, String email, String password, RoleName roleName) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn("Registration refused: email already in use '{}'", normalizedEmail);
            throw new IllegalArgumentException("email already in use");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("role not found: " + roleName));

        User user = User.builder()
                .name(name)
                .email(normalizedEmail) // entity will normalize again on @PrePersist (idempotent)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(role))
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: email='{}', roles={}", saved.getEmail(), saved.getRoles().stream().map(r -> r.getName().name()).toList());
        return saved;
    }

    /**
     * Authenticates a user by email + password.
     * Returns the full user (use findWithRoles to avoid N+1 when issuing tokens).
     */
    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();

        User user = userRepository.findWithRolesByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found '{}'", normalizedEmail);
                    return new UsernameNotFoundException("user not found");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Login failed: invalid password for '{}'", normalizedEmail);
            throw new BadCredentialsException("invalid credentials");
        }

        log.info("Login success: email='{}', roles={}", user.getEmail(), user.getRoles().stream().map(r -> r.getName().name()).toList());
        return user;
    }
}
