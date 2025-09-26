package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"teachers", "traitEvaluations"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Full name of the student */
    @Column(name = "name")
    private String name;

    /** National ID (keeps DB column 'cpf' to avoid migration) */
    @Column(name = "cpf", nullable = false, unique = true)
    private String nationalId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    /** Trait evaluations for this student */
    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<TraitEvaluation> traitEvaluations = new HashSet<>();

    /** Teachers associated to this student */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_teacher",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    @JsonManagedReference
    @Builder.Default
    private Set<Teacher> teachers = new HashSet<>();

    /* --- convenience helpers to keep the bidirectional association in sync (optional) --- */
    public void addTeacher(Teacher teacher) {
        this.teachers.add(teacher);
        // if Teacher has a mappedBy= "students", keep the other side in sync:
        // teacher.getStudents().add(this);
    }

    public void removeTeacher(Teacher teacher) {
        this.teachers.remove(teacher);
        // if Teacher has a mappedBy= "students":
        // teacher.getStudents().remove(this);
    }
}
