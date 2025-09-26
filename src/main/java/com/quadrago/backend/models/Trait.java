package com.quadrago.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "traits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"teacher", "traitEvaluations"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Trait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    /** Trait owner */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /** Evaluations that reference this trait */
    @OneToMany(
            mappedBy = "trait",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<TraitEvaluation> traitEvaluations = new HashSet<>();
}
