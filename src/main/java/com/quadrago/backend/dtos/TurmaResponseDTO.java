package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.NivelTurma;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.models.HorarioAula;
import com.quadrago.backend.models.Turma;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class TurmaResponseDTO {

    private Long id;
    private String nome;
    private NivelTurma nivel;
    private ProfessorResumoDTO professor;
    private Set<AlunoResumoDTO> alunos;
    private Set<HorarioAula> horarios;
    private NivelTurma classificacao;

    public TurmaResponseDTO(Turma turma) {
        this.id = turma.getId();
        this.nome = turma.getNome();
        this.nivel = turma.getNivel();
        this.professor = new ProfessorResumoDTO(turma.getProfessor());
        this.alunos = turma.getAlunos().stream()
                .map(AlunoResumoDTO::new)
                .collect(Collectors.toSet());
        this.horarios = turma.getHorarios();

        // Calcula a classificação com base na média de pontuação dos alunos
        this.classificacao = calcularClassificacao(turma);
    }

    private NivelTurma calcularClassificacao(Turma turma) {
        Set<Aluno> alunos = turma.getAlunos();

        if (alunos.isEmpty()) return NivelTurma.INICIANTE;

        double media = alunos.stream()
                .mapToInt(aluno -> aluno.getPontuacao() != null ? aluno.getPontuacao() : 0)
                .average()
                .orElse(0.0);

        if (media <= 3) return NivelTurma.INICIANTE;
        if (media <= 7) return NivelTurma.INTERMEDIARIO;
        return NivelTurma.AVANCADO;
    }
}
