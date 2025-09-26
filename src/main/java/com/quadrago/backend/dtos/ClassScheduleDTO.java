package com.quadrago.backend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassScheduleDTO {

    @NotNull(message = "dayOfWeek is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotNull(message = "duration is required")
    private Duration duration;
}
