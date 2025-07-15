package com.quadrago.backend.config;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.Role;
import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.RoleRepository;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cria roles se não existirem
        createRoleIfNotFound(RoleName.ADMIN);
        createRoleIfNotFound(RoleName.PROFESSOR);

        // Cria usuário admin se não existir
        createUserIfNotFound("admin", "admin@quadrago.com", "admin123", RoleName.ADMIN);

        // Cria usuário professor se não existir
        createUserIfNotFound("professor", "professor@quadrago.com", "professor123", RoleName.PROFESSOR);
    }

    private void createRoleIfNotFound(RoleName roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }

    private void createUserIfNotFound(String username, String email, String rawPassword, RoleName roleName) {
        if (userRepository.findByEmail(email).isEmpty()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role " + roleName + " não encontrada no banco"));

            User user = new User();
            user.setName(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));

            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);

            System.out.println("Usuário " + username + " criado com sucesso.");
        }
    }
}
