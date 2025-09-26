package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassGroupDTO {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private Level level;

    @NotNull
    private Long teacherId;

    @Builder.Default
    private Set<Long> studentIds = Set.of();

    @Builder.Default
    private Set<ClassScheduleDTO> schedules = Set.of();
}
