package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "students",
        uniqueConstraints = { @UniqueConstraint(name = "uk_students_cpf", columnNames = "cpf") },
        indexes = { @Index(name = "idx_students_cpf", columnList = "cpf") }
)
@PrimaryKeyJoinColumn(name = "id", foreignKey = @ForeignKey(name = "fk_students_user"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"teachers", "traitEvaluations"})
public class Student extends User {

    /** National ID (mantém coluna 'cpf') */
    @Column(name = "cpf", length = 11, nullable = false)
    private String nationalId;

    /** Telefone do aluno (somente dígitos normalizados no service) */
    @Column(name = "phone", length = 11)
    private String phone;

    /** Avaliações de traços do aluno */
    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<TraitEvaluation> traitEvaluations = new HashSet<>();

    /** Professores associados a este aluno */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_teacher",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    @JsonManagedReference
    @Builder.Default
    private Set<Teacher> teachers = new HashSet<>();

    /* --- helpers opcionais para manter a associação em sincronia --- */
    public void addTeacher(Teacher teacher) {
        this.teachers.add(teacher);
        // teacher.getStudents().add(this); // se quiser sincronizar o outro lado
    }

    public void removeTeacher(Teacher teacher) {
        this.teachers.remove(teacher);
        // teacher.getStudents().remove(this);
    }
}
