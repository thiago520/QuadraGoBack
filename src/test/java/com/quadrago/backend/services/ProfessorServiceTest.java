package com.quadrago.backend.services;

import com.quadrago.backend.dtos.ProfessorDTO;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ProfessorServiceTest {
    @Mock
    private ProfessorRepository repository;

    @InjectMocks
    private ProfessorService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveSalvarProfessor() {
        ProfessorDTO dto = new ProfessorDTO();
        dto.setNome("Carlos");
        dto.setTelefone("11999999999");
        dto.setCpf("12345678901");

        Professor salvo = new Professor(1L, dto.getNome(), dto.getTelefone(), dto.getCpf());

        when(repository.save(any())).thenReturn(salvo);

        Professor resultado = service.salvar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Carlos");
    }

    @Test
    void deveListarProfessores() {
        Professor p1 = new Professor(1L, "Carlos", "111", "123");
        Professor p2 = new Professor(2L, "Ana", "222", "456");

        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Professor> lista = service.listar();

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getNome()).isEqualTo("Carlos");
    }

    @Test
    void deveBuscarPorId() {
        Professor p = new Professor(1L, "Carlos", "111", "123");
        when(repository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Professor> resultado = service.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertThat(resultado.get().getNome()).isEqualTo("Carlos");
    }

    @Test
    void deveAtualizarProfessor() {
        Professor existente = new Professor(1L, "Carlos", "111", "123");
        ProfessorDTO dto = new ProfessorDTO();
        dto.setNome("Carlos Atualizado");
        dto.setTelefone("999");
        dto.setCpf("789");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Optional<Professor> resultado = service.atualizar(1L, dto);

        assertTrue(resultado.isPresent());
        assertThat(resultado.get().getNome()).isEqualTo("Carlos Atualizado");
    }

    @Test
    void deveDeletarPorId() {
        Long id = 1L;
        Professor professor = new Professor(id, "Nome", "123", "000");

        when(repository.findById(id)).thenReturn(Optional.of(professor));
        doNothing().when(repository).delete(professor);

        boolean resultado = service.deletar(id);

        assertTrue(resultado);
        verify(repository).findById(id);
        verify(repository).delete(professor);
    }
}
