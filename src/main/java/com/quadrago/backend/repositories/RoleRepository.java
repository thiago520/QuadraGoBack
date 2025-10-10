package com.quadrago.backend.repositories;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {
    Optional<Role> findByName(RoleName name);
}