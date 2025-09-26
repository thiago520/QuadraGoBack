package com.quadrago.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "ix_users_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "roles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Full name */
    @Column(name = "name", length = 150)
    private String name;

    /** Login/email (normalized to lowercase) */
    @Email
    @NotBlank
    @Column(name = "email", nullable = false, length = 180)
    private String email;

    /** BCrypt (recommended) or similar hash */
    @NotBlank
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** User roles (authorities) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false)
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /* --- convenience helpers --- */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /* --- normalize email before save/update --- */
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }
}
