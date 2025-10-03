package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = { @UniqueConstraint(name = "uk_users_email", columnNames = "email") },
        indexes = { @Index(name = "ix_users_email", columnList = "email") }
)
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = "roles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", length = 150)
    private String name;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @NotBlank
    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false)
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) { this.roles.add(role); }
    public void removeRole(Role role) { this.roles.remove(role); }

    @PrePersist @PreUpdate
    private void normalize() {
        if (this.email != null) this.email = this.email.trim().toLowerCase();
        if (this.name != null)  this.name = this.name.trim();
    }
}
