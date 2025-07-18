package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Professor;
import lombok.Data;

@Data
public class ProfessorResponseDTO {

    private Long id;
    private String nome;

    public ProfessorResponseDTO(Professor professor) {
        this.id = professor.getId();
        this.nome = professor.getNome();
    }
}
