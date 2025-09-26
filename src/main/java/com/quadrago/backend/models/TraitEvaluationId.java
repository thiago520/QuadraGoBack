package com.quadrago.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TraitEvaluationId implements Serializable {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "trait_id", nullable = false)
    private Long traitId;
}
