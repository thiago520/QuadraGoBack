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

@ExtendWith(MockitoExtension.class)  // inicializa os mocks automaticamente
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private AlunoService alunoService;

    // removeu setUp(), pois @ExtendWith(MockitoExtension.class) já inicializa os mocks

    @Test
    void deveSalvarAluno() {
        Set<Long> idsProfessores = Set.of(1L);
        AlunoDTO dto = new AlunoDTO("Joao", "12345678901", "joao@email.com", "9999999999", 5, idsProfessores);

        when(professorRepository.findAllById(idsProfessores)).thenReturn(new ArrayList<>());

        Aluno alunoSalvo = Aluno.builder()
                .id(1L)
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
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
        List<Aluno> alunos = Arrays.asList(
                Aluno.builder().id(1L).nome("A").email("a@email.com").cpf("11111111111").telefone("9999999999").professores(new HashSet<>()).build(),
                Aluno.builder().id(2L).nome("B").email("b@email.com").cpf("22222222222").telefone("8888888888").professores(new HashSet<>()).build()
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
                .email("j@email.com")
                .cpf("12388888888")
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
                .email("j@email.com")
                .cpf("12333333333")
                .telefone("9999999999")
                .professores(new HashSet<>())
                .build();

        Set<Long> novosProfessores = Set.of(2L);
        AlunoDTO dto = new AlunoDTO("Novo Nome", "99999999999", "novo@email.com", "8888888888", 5, novosProfessores);

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
                .email("j@email.com")
                .cpf("12345678901")
                .telefone("9990000000")
                .professores(new HashSet<>())
                .build();

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));

        boolean resultado = alunoService.deletar(1L);

        assertTrue(resultado);
        verify(alunoRepository, times(1)).delete(aluno);
    }

    @Test
    void deveSalvarAlunoComProfessores() {
        AlunoDTO dto = new AlunoDTO("Ana", "12345678900", "ana@email.com", "11988887777", 5, Set.of(1L, 2L));

        Professor p1 = new Professor(1L, "Carlos", "11999999999", "12345678901", new HashSet<>());
        Professor p2 = new Professor(2L, "Marcia", "21988888888", "09876543210", new HashSet<>());

        when(professorRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(p1, p2));

        Aluno salvo = new Aluno(null, dto.getNome(), dto.getCpf(), dto.getEmail(), dto.getTelefone(), 5, Set.of(p1, p2));
        when(alunoRepository.save(any(Aluno.class))).thenReturn(salvo);

        Aluno resultado = alunoService.salvar(dto);

        assertEquals("Ana", resultado.getNome());
        assertEquals(2, resultado.getProfessores().size());
        verify(professorRepository).findAllById(dto.getProfessoresIds());
        verify(alunoRepository).save(any(Aluno.class));
    }

}