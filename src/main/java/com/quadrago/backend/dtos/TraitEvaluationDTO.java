package com.quadrago.backend.dtos;

import com.quadrago.backend.models.TraitEvaluation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitEvaluationDTO {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "traitId is required")
    private Long traitId;

    @NotNull(message = "score is required")
    @Min(value = 0, message = "score must be >= 0")
    @Max(value = 10, message = "score must be <= 10")
    private Integer score;

    /** Convenience ctor from entity */
    public TraitEvaluationDTO(TraitEvaluation evaluation) {
        this.studentId = evaluation.getStudent() != null ? evaluation.getStudent().getId() : null;
        this.traitId   = evaluation.getTrait()   != null ? evaluation.getTrait().getId()   : null;
        this.score     = evaluation.getScore();
    }
}
