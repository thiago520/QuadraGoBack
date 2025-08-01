package com.quadrago.backend.services;

import com.quadrago.backend.dtos.HorarioAulaDTO;
import com.quadrago.backend.dtos.TurmaDTO;
import com.quadrago.backend.dtos.TurmaResponseDTO;
import com.quadrago.backend.enums.Nivel;
import com.quadrago.backend.models.*;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import com.quadrago.backend.repositories.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private TurmaService turmaService;

    private Aluno criarAlunoComNotas(int... notas) {
        Aluno aluno = new Aluno();
        aluno.setId(UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE);
        Set<AvaliacaoCaracteristica> avaliacoes = new HashSet<>();
        for (int nota : notas) {
            AvaliacaoCaracteristica a = new AvaliacaoCaracteristica();
            a.setNota(nota);
            a.setAluno(aluno);
            a.setCaracteristica(new Caracteristica()); // necessário, mas não relevante para o cálculo da média
            avaliacoes.add(a);
        }
        aluno.setAvaliacoes(avaliacoes);
        return aluno;
    }

    @Test
    void deveSalvarTurmaComSucesso() {
        TurmaDTO dto = new TurmaDTO();
        dto.setNome("Turma A");
        dto.setNivel(Nivel.INTERMEDIARIO);
        dto.setProfessorId(1L);
        dto.setAlunosIds(Set.of(1L, 2L));
        dto.setHorarios(Set.of(new HorarioAulaDTO(DayOfWeek.TUESDAY, LocalTime.of(10, 0), Duration.ofHours(1))));

        Professor professor = new Professor();
        professor.setId(1L);

        Aluno aluno1 = criarAlunoComNotas(4, 5);
        Aluno aluno2 = criarAlunoComNotas(6, 7);

        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(alunoRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(aluno1, aluno2));
        when(turmaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TurmaResponseDTO response = turmaService.salvar(dto);

        assertEquals("Turma A", response.getNome());
        assertEquals(Nivel.INTERMEDIARIO, response.getClassificacao());
    }

    @Test
    void deveAtualizarTurma() {
        Long id = 1L;

        Turma turmaExistente = new Turma();
        turmaExistente.setId(id);
        turmaExistente.setNome("Antigo");
        turmaExistente.setAlunos(new HashSet<>());
        turmaExistente.setHorarios(new HashSet<>());

        TurmaDTO dto = new TurmaDTO();
        dto.setNome("Nova Turma");
        dto.setNivel(Nivel.AVANCADO);
        dto.setProfessorId(2L);
        dto.setAlunosIds(Set.of(1L));
        dto.setHorarios(Set.of(new HorarioAulaDTO(DayOfWeek.THURSDAY, LocalTime.of(14, 0), Duration.ofMinutes(90))));

        Professor professor = new Professor();
        professor.setId(2L);

        Aluno aluno = criarAlunoComNotas(9, 10);

        when(turmaRepository.findById(id)).thenReturn(Optional.of(turmaExistente));
        when(professorRepository.findById(2L)).thenReturn(Optional.of(professor));
        when(alunoRepository.findAllById(Set.of(1L))).thenReturn(List.of(aluno));
        when(turmaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TurmaResponseDTO atualizada = turmaService.atualizar(id, dto);

        assertEquals("Nova Turma", atualizada.getNome());
        assertEquals(Nivel.AVANCADO, atualizada.getClassificacao());
    }

    @Test
    void deveDeletarTurmaComSucesso() {
        Turma turma = new Turma();
        turma.setId(1L);

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        boolean deletado = turmaService.deletar(1L);

        assertTrue(deletado);
        verify(turmaRepository).delete(turma);
    }

    @Test
    void deveRetornarFalseAoDeletarTurmaInexistente() {
        when(turmaRepository.findById(99L)).thenReturn(Optional.empty());

        boolean deletado = turmaService.deletar(99L);

        assertFalse(deletado);
    }

    @Test
    void deveBuscarTurmaPorIdComSucesso() {
        Turma turma = new Turma();
        turma.setId(1L);
        turma.setNome("Turma X");
        turma.setNivel(Nivel.INICIANTE);
        turma.setProfessor(new Professor());
        turma.setAlunos(Set.of());
        turma.setHorarios(Set.of());

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        TurmaResponseDTO dto = turmaService.buscarPorId(1L);

        assertEquals("Turma X", dto.getNome());
    }

    @Test
    void deveLancarExcecaoQuandoTurmaNaoEncontrada() {
        when(turmaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> turmaService.buscarPorId(404L));
    }

    @Test
    void deveRetornarTodasAsTurmas() {
        Turma t1 = new Turma();
        t1.setId(1L);
        t1.setNome("T1");
        t1.setProfessor(new Professor());
        t1.setAlunos(Set.of());
        t1.setHorarios(Set.of());

        Turma t2 = new Turma();
        t2.setId(2L);
        t2.setNome("T2");
        t2.setProfessor(new Professor());
        t2.setAlunos(Set.of());
        t2.setHorarios(Set.of());

        when(turmaRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TurmaResponseDTO> resultado = turmaService.listar();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveCalcularClassificacaoComMediaAlta() {
        Aluno a1 = criarAlunoComNotas(9, 10);
        Aluno a2 = criarAlunoComNotas(8, 9);

        Turma turma = new Turma();
        turma.setAlunos(Set.of(a1, a2));

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        Optional<Nivel> classificacao = turmaService.calcularNivelTurma(1L);

        assertTrue(classificacao.isPresent());
        assertEquals(Nivel.AVANCADO, classificacao.get());
    }

    @Test
    void deveClassificarComoInicianteSemAlunos() {
        Turma turma = new Turma();
        turma.setAlunos(Set.of());

        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        Optional<Nivel> nivel = turmaService.calcularNivelTurma(1L);

        assertTrue(nivel.isPresent());
        assertEquals(Nivel.INICIANTE, nivel.get());
    }
}
