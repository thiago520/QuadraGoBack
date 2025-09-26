package com.quadrago.backend.config;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.Role;
import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.RoleRepository;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@Profile("!test") // não executar no profile de testes
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Cria roles se não existirem
        createRoleIfNotFound(RoleName.ADMIN);
        createRoleIfNotFound(RoleName.TEACHER); // manter enum existente

        // Cria usuários básicos, se não existirem
        createUserIfNotFound("admin", "admin@quadrago.com", "admin123", RoleName.ADMIN);
        createUserIfNotFound("teacher", "teacher@quadrago.com", "professor123", RoleName.TEACHER);
    }

    private void createRoleIfNotFound(RoleName roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = Role.builder().name(roleName).build();
            Role saved = roleRepository.save(role);
            log.info("Role {} criada.", roleName);
            return saved;
        });
    }

    private void createUserIfNotFound(String username, String email, String rawPassword, RoleName roleName) {
        userRepository.findByEmail(email.toLowerCase()).ifPresentOrElse(existing -> {
            log.debug("Usuário {} já existe. Nenhuma alteração aplicada.", email);
        }, () -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Role " + roleName + " não encontrada"));

            User user = User.builder()
                    .name(username)
                    .email(email) // será normalizado para lowercase pelo @PrePersist da entidade
                    .password(passwordEncoder.encode(rawPassword))
                    .roles(Set.of(role))
                    .build();

            userRepository.save(user);
            System.out.println("Usuário " + username + " criado com sucesso.");
            log.info("Usuário {} criado com role {}.", email, roleName);
        });
    }
}
