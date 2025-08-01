package com.quadrago.backend.services;

import com.quadrago.backend.dtos.AvaliacaoCaracteristicaDTO;
import com.quadrago.backend.models.*;
import com.quadrago.backend.repositories.AvaliacaoCaracteristicaRepository;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.CaracteristicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvaliacaoCaracteristicaServiceTest {

    @Mock
    private AvaliacaoCaracteristicaRepository avaliacaoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private CaracteristicaRepository caracteristicaRepository;

    @InjectMocks
    private AvaliacaoCaracteristicaService avaliacaoService;

    private Aluno aluno;
    private Caracteristica caracteristica;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        aluno = Aluno.builder()
                .id(1L)
                .nome("João")
                .cpf("12345678900")
                .email("joao@email.com")
                .telefone("11999999999")
                .professores(new HashSet<>())
                .avaliacoes(new HashSet<>())
                .build();

        Professor professor = Professor.builder()
                .id(1L)
                .nome("Prof. Teste")
                .telefone("11988887777")
                .cpf("09876543210")
                .build();

        caracteristica = Caracteristica.builder()
                .id(2L)
                .nome("Resistência")
                .professor(professor)
                .build();
    }

    @Test
    void deveSalvarAvaliacao() {
        AvaliacaoCaracteristicaDTO dto = new AvaliacaoCaracteristicaDTO();
        dto.setAlunoId(aluno.getId());
        dto.setCaracteristicaId(caracteristica.getId());
        dto.setNota(7);

        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(caracteristicaRepository.findById(caracteristica.getId())).thenReturn(Optional.of(caracteristica));

        AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId(aluno.getId(), caracteristica.getId());

        AvaliacaoCaracteristica avaliacaoSalva = AvaliacaoCaracteristica.builder()
                .id(id)
                .aluno(aluno)
                .caracteristica(caracteristica)
                .nota(dto.getNota())
                .build();

        when(avaliacaoRepository.save(any())).thenReturn(avaliacaoSalva);

        AvaliacaoCaracteristica resultado = avaliacaoService.salvar(dto);

        assertNotNull(resultado);
        assertEquals(7, resultado.getNota());
        assertEquals(aluno.getId(), resultado.getAluno().getId());
        assertEquals(caracteristica.getId(), resultado.getCaracteristica().getId());
    }

    @Test
    void deveAtualizarNota() {
        AvaliacaoCaracteristicaDTO dto = new AvaliacaoCaracteristicaDTO();
        dto.setAlunoId(aluno.getId());
        dto.setCaracteristicaId(caracteristica.getId());
        dto.setNota(9);

        AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId(aluno.getId(), caracteristica.getId());

        AvaliacaoCaracteristica existente = AvaliacaoCaracteristica.builder()
                .id(id)
                .aluno(aluno)
                .caracteristica(caracteristica)
                .nota(5)
                .build();

        when(avaliacaoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(avaliacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoCaracteristica atualizado = avaliacaoService.atualizar(dto);

        assertNotNull(atualizado);
        assertEquals(9, atualizado.getNota());
    }

    @Test
    void deveDeletarAvaliacao() {
        AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId(1L, 2L);

        AvaliacaoCaracteristica existente = AvaliacaoCaracteristica.builder()
                .id(id)
                .aluno(aluno)
                .caracteristica(caracteristica)
                .nota(8)
                .build();

        when(avaliacaoRepository.findById(id)).thenReturn(Optional.of(existente));

        boolean resultado = avaliacaoService.deletar(1L, 2L);

        assertTrue(resultado);
        verify(avaliacaoRepository, times(1)).delete(existente);
    }

    @Test
    void deveRetornarFalse_QuandoAvaliacaoNaoExistirAoDeletar() {
        when(avaliacaoRepository.findById(any())).thenReturn(Optional.empty());

        boolean resultado = avaliacaoService.deletar(999L, 888L);

        assertFalse(resultado);
        verify(avaliacaoRepository, never()).delete(any());
    }
}
