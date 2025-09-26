package com.quadrago.backend.services;

import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();

        User user = userRepository.findWithRolesByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("User not found: '{}'", normalizedEmail);
                    return new UsernameNotFoundException("user not found");
                });

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                .collect(Collectors.toSet());

        log.debug("Loaded user details: email='{}', roles={}", user.getEmail(),
                user.getRoles().stream().map(r -> r.getName().name()).toList());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
