package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.Nivel;
import lombok.Data;

import java.util.Set;

@Data
public class TurmaDTO {
    private String nome;
    private Nivel nivel;
    private Long professorId;
    private Set<Long> alunosIds;
    private Set<HorarioAulaDTO> horarios;
}