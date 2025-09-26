package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
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
    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    /** National ID; keeps DB column 'cpf' to avoid migration now */
    @Column(name = "cpf", unique = true)
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
        // if you later add mappedBy="students" in Student side sync there:
        // s.getTeachers().add(this);
    }

    public void removeStudent(Student s) {
        this.students.remove(s);
        // s.getTeachers().remove(this);
    }
}
