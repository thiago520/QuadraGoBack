package com.quadrago.backend.filters;

import com.quadrago.backend.enums.UserStatus;
import com.quadrago.backend.models.Role;
import com.quadrago.backend.models.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Principal com id exposto para uso em @PreAuthorize (principal.id).
 */
@Getter
@EqualsAndHashCode(of = "id")
public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;         // email
    private final String password;         // hash
    private final Set<GrantedAuthority> authorities;

    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;
    private final boolean accountNonLocked;
    private final boolean enabled;

    private CustomUserPrincipal(Long id,
                                String username,
                                String password,
                                Set<GrantedAuthority> authorities,
                                boolean accountNonExpired,
                                boolean credentialsNonExpired,
                                boolean accountNonLocked,
                                boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }

    public static CustomUserPrincipal fromEntity(User u) {
        Set<GrantedAuthority> auths = mapAuthorities(u.getRoles());

        boolean enabled = u.getStatus() == UserStatus.ACTIVE;
        boolean accountNonLocked = u.getStatus() != UserStatus.BLOCKED;

        return new CustomUserPrincipal(
                u.getId(),
                u.getEmail().toLowerCase(),
                u.getPasswordHash(),
                auths,
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                accountNonLocked,
                enabled
        );
    }

    private static Set<GrantedAuthority> mapAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
