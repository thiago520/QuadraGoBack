package com.quadrago.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import com.quadrago.backend.dtos.AlunoDTO;
import com.quadrago.backend.dtos.AlunoResponseDTO;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private AlunoService alunoService;

    @Test
    void deveSalvarAluno() {
        Set<Long> idsProfessores = Set.of(1L);
        AlunoDTO dto = new AlunoDTO("Joao", "12345678901", "joao@email.com", "9999999999", idsProfessores);

        when(professorRepository.findAllById(idsProfessores)).thenReturn(new ArrayList<>());

        Aluno alunoSalvo = Aluno.builder()
                .id(1L)
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .professores(new HashSet<>())
                .build();

        when(alunoRepository.save(any())).thenReturn(alunoSalvo);

        Aluno resultado = alunoService.salvar(dto);

        assertNotNull(resultado);
        assertEquals("Joao", resultado.getNome());
    }

    @Test
    void deveListarAlunos() {
        List<Aluno> alunos = List.of(
                Aluno.builder().id(1L).nome("A").cpf("11111111111").email("a@email.com").telefone("9999999999").professores(new HashSet<>()).build(),
                Aluno.builder().id(2L).nome("B").cpf("22222222222").email("b@email.com").telefone("8888888888").professores(new HashSet<>()).build()
        );

        when(alunoRepository.findAll()).thenReturn(alunos);

        List<AlunoResponseDTO> resultado = alunoService.listar();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveBuscarAlunoPorId() {
        Aluno aluno = Aluno.builder()
                .id(1L)
                .nome("Joao")
                .cpf("12388888888")
                .email("j@email.com")
                .telefone("9999999999")
                .professores(new HashSet<>())
                .build();

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));

        Optional<Aluno> resultado = alunoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Joao", resultado.get().getNome());
    }

    @Test
    void deveAtualizarAluno() {
        Aluno aluno = Aluno.builder()
                .id(1L)
                .nome("Joao")
                .cpf("12333333333")
                .email("j@email.com")
                .telefone("9999999999")
                .professores(new HashSet<>())
                .build();

        Set<Long> novosProfessores = Set.of(2L);
        AlunoDTO dto = new AlunoDTO("Novo Nome", "99999999999", "novo@email.com", "8888888888", novosProfessores);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(professorRepository.findAllById(novosProfessores)).thenReturn(new ArrayList<>());
        when(alunoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Aluno> resultado = alunoService.atualizar(1L, dto);

        assertTrue(resultado.isPresent());
        assertEquals("Novo Nome", resultado.get().getNome());
    }

    @Test
    void deveDeletarAluno() {
        Aluno aluno = Aluno.builder()
                .id(1L)
                .nome("Joao")
                .cpf("12345678901")
                .email("j@email.com")
                .telefone("9990000000")
                .professores(new HashSet<>())
                .build();

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));

        boolean resultado = alunoService.deletar(1L);

        assertTrue(resultado);
        verify(alunoRepository).delete(aluno);
    }

    @Test
    void deveSalvarAlunoComProfessores() {
        AlunoDTO dto = new AlunoDTO("Ana", "12345678900", "ana@email.com", "11988887777", Set.of(1L, 2L));

        Professor p1 = Professor.builder()
                .id(1L)
                .nome("Carlos")
                .telefone("11999999999")
                .cpf("12345678901")
                .alunos(new HashSet<>()) // se necessário
                .build();

        Professor p2 = Professor.builder()
                .id(2L)
                .nome("Roberto")
                .telefone("11998789999")
                .cpf("12348778901")
                .alunos(new HashSet<>()) // se necessário
                .build();


        when(professorRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(p1, p2));

        Aluno alunoSalvo = Aluno.builder()
                .id(1L)
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .professores(Set.of(p1, p2))
                .build();

        when(alunoRepository.save(any())).thenReturn(alunoSalvo);

        Aluno resultado = alunoService.salvar(dto);

        assertEquals("Ana", resultado.getNome());
        assertEquals(2, resultado.getProfessores().size());
        verify(professorRepository).findAllById(dto.getProfessoresIds());
        verify(alunoRepository).save(any(Aluno.class));
    }
}
