package com.quadrago.backend.filters;

import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Usuário não informado");
        }
        String email = username.toLowerCase();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        var principal = CustomUserPrincipal.fromEntity(user);
        log.debug("Loaded user: id={}, email={}, roles={}", principal.getId(), principal.getUsername(), principal.getAuthorities());
        return principal;
    }

    /**
     * Opcional: útil para montar Authentication a partir do ID (refresh, etc).
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: id=" + id));
        return CustomUserPrincipal.fromEntity(user);
    }
}
