package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Trait;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitDTO {

    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "teacherId is required")
    private Long teacherId;

    /** Convenience ctor from entity */
    public TraitDTO(Trait trait) {
        this.id = trait.getId();
        this.name = trait.getName();
        this.teacherId = trait.getTeacher() != null ? trait.getTeacher().getId() : null;
    }
}
