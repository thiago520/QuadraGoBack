package com.quadrago.backend.repositories;

import com.quadrago.backend.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Case-insensitive lookup (recomendado para autenticação) */
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmail(String email);

    /** Validação de duplicidade no cadastro */
    boolean existsByEmailIgnoreCase(String email);

    /** Busca o usuário já com roles (evita N+1) */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByEmailIgnoreCase(String email);
}
