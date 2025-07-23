package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Aluno;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AlunoResponseDTO {
    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Set<ProfessorResumoDTO> professores;

    public AlunoResponseDTO(Aluno aluno) {
        this.id = aluno.getId();
        this.nome = aluno.getNome();
        this.cpf = aluno.getCpf();
        this.email = aluno.getEmail();
        this.telefone = aluno.getTelefone();
        this.professores = aluno.getProfessores().stream()
                .map(ProfessorResumoDTO::new)
                .collect(Collectors.toSet());
    }
}
