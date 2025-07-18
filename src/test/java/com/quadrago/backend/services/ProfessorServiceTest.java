package com.quadrago.backend.services;

import com.quadrago.backend.dtos.ProfessorDTO;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.ProfessorRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfessorServiceTest {

    @Mock
    private ProfessorRepository repository;

    @InjectMocks
    private ProfessorService service;

    @Test
    void deveSalvarProfessor() {
        ProfessorDTO dto = new ProfessorDTO();
        dto.setNome("Carlos");
        dto.setTelefone("11999999999");
        dto.setCpf("12345678901");

        Professor salvo = Professor.builder()
                .id(1L)
                .nome(dto.getNome())
                .telefone(dto.getTelefone())
                .cpf(dto.getCpf())
                .build();

        when(repository.save(any(Professor.class))).thenReturn(salvo);

        Professor resultado = service.salvar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Carlos");

        verify(repository).save(any(Professor.class));
    }

    @Test
    void deveListarProfessores() {
        Professor p1 = Professor.builder()
                .id(1L)
                .nome("Carlos")
                .telefone("111")
                .cpf("123")
                .build();

        Professor p2 = Professor.builder()
                .id(2L)
                .nome("Ana")
                .telefone("222")
                .cpf("456")
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Professor> lista = service.listar();

        assertThat(lista).hasSize(2);
        assertThat(lista).extracting(Professor::getNome).containsExactly("Carlos", "Ana");

        verify(repository).findAll();
    }

    @Test
    void deveBuscarPorId() {
        Professor p = Professor.builder()
                .id(1L)
                .nome("Carlos")
                .telefone("111")
                .cpf("123")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Professor> resultado = service.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertThat(resultado.get().getNome()).isEqualTo("Carlos");

        verify(repository).findById(1L);
    }

    @Test
    void deveAtualizarProfessor() {
        Professor existente = Professor.builder()
                .id(1L)
                .nome("Carlos")
                .telefone("111")
                .cpf("123")
                .build();

        ProfessorDTO dto = new ProfessorDTO();
        dto.setNome("Carlos Atualizado");
        dto.setTelefone("999");
        dto.setCpf("789");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Professor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Professor> resultado = service.atualizar(1L, dto);

        assertTrue(resultado.isPresent());
        assertThat(resultado.get().getNome()).isEqualTo("Carlos Atualizado");

        verify(repository).findById(1L);
        verify(repository).save(any(Professor.class));
    }

    @Test
    void deveDeletarPorId() {
        Long id = 1L;

        Professor professor = Professor.builder()
                .id(id)
                .nome("Nome")
                .telefone("123")
                .cpf("000")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(professor));
        doNothing().when(repository).delete(professor);

        boolean resultado = service.deletar(id);

        assertTrue(resultado);

        verify(repository).findById(id);
        verify(repository).delete(professor);
    }
}
