package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.Nivel;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.models.HorarioAula;
import com.quadrago.backend.models.Turma;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class TurmaResponseDTO {

    private Long id;
    private String nome;
    private Nivel nivel;
    private ProfessorResumoDTO professor;
    private Set<AlunoResumoDTO> alunos;
    private Set<HorarioAula> horarios;
    private Nivel classificacao;

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

    private Nivel calcularClassificacao(Turma turma) {
        Set<Aluno> alunos = turma.getAlunos();

        if (alunos.isEmpty()) return Nivel.INICIANTE;

        // Calcula a média de todas as notas de todos os alunos
        List<Integer> todasNotas = alunos.stream()
                .flatMap(aluno -> aluno.getAvaliacoes().stream())
                .map(avaliacao -> avaliacao.getNota() != null ? avaliacao.getNota() : 0)
                .toList();

        if (todasNotas.isEmpty()) return Nivel.INICIANTE;

        double media = todasNotas.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        if (media <= 3) return Nivel.INICIANTE;
        if (media <= 7) return Nivel.INTERMEDIARIO;
        return Nivel.AVANCADO;
    }

}
