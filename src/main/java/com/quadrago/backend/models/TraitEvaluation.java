package com.quadrago.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "trait_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"student", "trait"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TraitEvaluation {

    @EmbeddedId
    @Builder.Default
    @EqualsAndHashCode.Include
    private TraitEvaluationId id = new TraitEvaluationId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentId") // <-- nome do campo no ID, não o nome da coluna
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("traitId")   // <-- nome do campo no ID
    @JoinColumn(name = "trait_id", nullable = false)
    private Trait trait;

    @Min(0)
    @Max(10)
    @Column(name = "score", nullable = false)
    private Integer score;

    /* --- convenience factory (opcional) --- */
    public static TraitEvaluation of(Student student, Trait trait, int score) {
        return TraitEvaluation.builder()
                .student(student)
                .trait(trait)
                .score(score)
                .build();
    }

    /* Sincroniza o EmbeddedId quando setar associações via builder ou setters */
    @PostLoad
    @PostPersist
    @PostUpdate
    private void syncId() {
        if (id == null) id = new TraitEvaluationId();
        if (student != null) id.setStudentId(student.getId());
        if (trait != null)   id.setTraitId(trait.getId());
    }
}
