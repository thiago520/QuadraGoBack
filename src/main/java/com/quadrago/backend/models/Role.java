package com.quadrago.backend.models;

import com.quadrago.backend.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Role identifier (e.g., ADMIN, TEACHER, STUDENT) */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 50)
    private RoleName name;
}
