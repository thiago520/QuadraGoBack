package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Aluno;
import lombok.Data;

@Data
public class AlunoResumoDTO {
    private Long id;
    private String nome;

    public AlunoResumoDTO(Aluno aluno) {
        this.id = aluno.getId();
        this.nome = aluno.getNome();
    }
}