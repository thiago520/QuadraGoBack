package com.quadrago.backend.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAula {
    private DayOfWeek diaSemana;  // java.time.DayOfWeek
    private LocalTime horarioInicio;
    private Duration duracao;
}