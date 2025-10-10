package com.quadrago.backend.models;

import com.quadrago.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "party_id", unique = true, nullable = false)
    private Party party;

    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private UserStatus status = UserStatus.ACTIVE;

    private OffsetDateTime lastLoginAt;
    @Column(nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PreUpdate void touch() { this.updatedAt = OffsetDateTime.now(); }
}
