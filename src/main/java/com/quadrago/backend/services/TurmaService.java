package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TurmaDTO;
import com.quadrago.backend.dtos.TurmaResponseDTO;
import com.quadrago.backend.enums.Nivel;
import com.quadrago.backend.models.*;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import com.quadrago.backend.repositories.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;

    public TurmaResponseDTO salvar(TurmaDTO dto) {
        Turma turma = new Turma();
        turma.setNome(dto.getNome());
        turma.setNivel(dto.getNivel());

        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado"));
        turma.setProfessor(professor);

        List<Aluno> alunos = alunoRepository.findAllById(dto.getAlunosIds());
        turma.setAlunos(Set.copyOf(alunos));

        Set<HorarioAula> horarios = dto.getHorarios().stream()
                .map(h -> new HorarioAula(h.getDiaSemana(), h.getHorario(), h.getDuracao()))
                .collect(Collectors.toSet());
        turma.setHorarios(horarios);

        Turma turmaSalva = turmaRepository.save(turma);
        return new TurmaResponseDTO(turmaSalva);
    }

    public List<TurmaResponseDTO> listar() {
        return turmaRepository.findAll().stream()
                .map(TurmaResponseDTO::new)
                .collect(Collectors.toList());
    }

    public TurmaResponseDTO atualizar(Long id, TurmaDTO dto) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Turma não encontrada"));

        turma.setNome(dto.getNome());
        turma.setNivel(dto.getNivel());

        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado"));
        turma.setProfessor(professor);

        List<Aluno> alunos = alunoRepository.findAllById(dto.getAlunosIds());
        turma.setAlunos(Set.copyOf(alunos));

        Set<HorarioAula> horarios = dto.getHorarios().stream()
                .map(h -> new HorarioAula(h.getDiaSemana(), h.getHorario(), h.getDuracao()))
                .collect(Collectors.toSet());
        turma.setHorarios(horarios);

        Turma turmaAtualizada = turmaRepository.save(turma);
        return new TurmaResponseDTO(turmaAtualizada);
    }

    public boolean deletar(Long id) {
        return turmaRepository.findById(id).map(t -> {
            turmaRepository.delete(t);
            return true;
        }).orElse(false);
    }

    public TurmaResponseDTO buscarPorId(Long id) {
        return turmaRepository.findById(id)
                .map(TurmaResponseDTO::new)
                .orElseThrow(() -> new NoSuchElementException("Turma não encontrada"));
    }

    public Optional<Nivel> calcularNivelTurma(Long turmaId) {
        return turmaRepository.findById(turmaId).map(turma -> {
            Set<Aluno> alunos = turma.getAlunos();

            if (alunos.isEmpty()) {
                return Nivel.INICIANTE; // padrão se não houver alunos
            }

            double media = alunos.stream()
                    .mapToInt(Aluno::getPontuacao)
                    .average()
                    .orElse(0.0);

            if (media <= 3) return Nivel.INICIANTE;
            if (media <= 7) return Nivel.INTERMEDIARIO;
            return Nivel.AVANCADO;
        });
    }

}
