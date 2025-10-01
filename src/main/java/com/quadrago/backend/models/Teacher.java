package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "teachers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teachers_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_teachers_cpf", columnNames = "cpf")
        },
        indexes = {
                @Index(name = "idx_teachers_email", columnList = "email"),
                @Index(name = "idx_teachers_cpf", columnList = "cpf")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"students", "traits"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Full name of the teacher */
    @NotBlank
    @Column(name = "name", nullable = false, length = 160)
    private String name;

    /** Public email for login/contato (unique) */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 11)
    private String phone;

    /** National ID; keeps DB column 'cpf' to avoid migration now */
    @Column(name = "cpf", length = 11)
    private String nationalId;

    /** Students associated to this teacher (inverse side of student_teacher) */
    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
    @JsonBackReference
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    /** Traits owned by this teacher */
    @OneToMany(
            mappedBy = "teacher",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<Trait> traits = new HashSet<>();

    /* --- convenience helpers for bidirectional sync (optional) --- */
    public void addStudent(Student s) {
        this.students.add(s);
        // s.getTeachers().add(this);
    }

    public void removeStudent(Student s) {
        this.students.remove(s);
        // s.getTeachers().remove(this);
    }
}
