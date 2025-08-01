package com.quadrago.backend.services;

import com.quadrago.backend.dtos.CaracteristicaDTO;
import com.quadrago.backend.models.Caracteristica;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.CaracteristicaRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaracteristicaServiceTest {

    @InjectMocks
    private CaracteristicaService caracteristicaService;

    @Mock
    private CaracteristicaRepository caracteristicaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Test
    void deveSalvarCaracteristica() {
        Professor professor = new Professor();
        professor.setId(1L);

        CaracteristicaDTO dto = new CaracteristicaDTO();
        dto.setNome("Dedicação");
        dto.setProfessorId(1L);

        Caracteristica caracteristicaSalva = new Caracteristica(1L, "Dedicação", professor, new HashSet<>());

        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(caracteristicaRepository.save(any())).thenReturn(caracteristicaSalva);

        CaracteristicaDTO resultado = caracteristicaService.salvar(dto);

        assertNotNull(resultado);
        assertEquals("Dedicação", resultado.getNome());
    }

    @Test
    void deveAtualizarCaracteristica() {
        Professor professor = new Professor();
        professor.setId(1L);

        Caracteristica existente = new Caracteristica(1L, "Foco", professor, new HashSet<>());

        CaracteristicaDTO dto = new CaracteristicaDTO();
        dto.setNome("Disciplina");
        dto.setProfessorId(1L);

        when(caracteristicaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(caracteristicaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CaracteristicaDTO atualizado = caracteristicaService.atualizar(1L, dto);

        assertEquals("Disciplina", atualizado.getNome());
        assertEquals(1L, atualizado.getProfessorId());
    }

    @Test
    void deveListarPorProfessor() {
        Professor professor = new Professor();
        professor.setId(1L);

        Caracteristica c1 = new Caracteristica(1L, "Força", professor, new HashSet<>());
        Caracteristica c2 = new Caracteristica(2L, "Agilidade", professor, new HashSet<>());

        when(caracteristicaRepository.findByProfessorId(1L)).thenReturn(List.of(c1, c2));

        List<CaracteristicaDTO> resultado = caracteristicaService.listarPorProfessor(1L);

        assertEquals(2, resultado.size());
        assertEquals("Força", resultado.get(0).getNome());
        assertEquals("Agilidade", resultado.get(1).getNome());
    }

    @Test
    void deveDeletarCaracteristica() {
        Caracteristica c = new Caracteristica();
        c.setId(1L);
        c.setNome("Comprometimento");

        when(caracteristicaRepository.findById(1L)).thenReturn(Optional.of(c));

        boolean resultado = caracteristicaService.deletar(1L);

        assertTrue(resultado);
        verify(caracteristicaRepository).delete(c);
    }

    @Test
    void deveRetornarFalse_AoDeletarCaracteristicaInexistente() {
        when(caracteristicaRepository.findById(99L)).thenReturn(Optional.empty());

        boolean resultado = caracteristicaService.deletar(99L);

        assertFalse(resultado);
    }
}
