package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "teachers",
        uniqueConstraints = { @UniqueConstraint(name = "uk_teachers_cpf", columnNames = "cpf") },
        indexes = { @Index(name = "idx_teachers_cpf", columnList = "cpf") }
)
@PrimaryKeyJoinColumn(name = "id", foreignKey = @ForeignKey(name = "fk_teachers_user"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"students", "traits"})
public class Teacher extends User {

    @Column(name = "phone", length = 11)
    private String phone;

    @Column(name = "cpf", length = 11)
    private String nationalId;

    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
    @JsonBackReference
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    @OneToMany(
            mappedBy = "teacher",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<Trait> traits = new HashSet<>();
}
