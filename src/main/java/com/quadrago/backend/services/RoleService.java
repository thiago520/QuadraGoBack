// com/quadrago/backend/services/RoleService.java
package com.quadrago.backend.services;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.Role;
import com.quadrago.backend.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getOrCreate(RoleName name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).description(name.name()).build()));
    }
}
