package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TraitDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.repositories.TraitRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraitServiceTest {

    @InjectMocks
    private TraitService traitService;

    @Mock
    private TraitRepository traitRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Test
    void shouldCreateTrait() {
        Teacher teacher = Teacher.builder().id(1L).build();

        TraitDTO dto = TraitDTO.builder()
                .name("Dedication")
                .teacherId(1L)
                .build();

        Trait saved = Trait.builder()
                .id(1L)
                .name("Dedication")
                .teacher(teacher)
                .traitEvaluations(new HashSet<>())
                .build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        // no duplicate for this teacher/name
        when(traitRepository.existsByTeacher_IdAndNameIgnoreCase(1L, "Dedication")).thenReturn(false);
        when(traitRepository.save(any(Trait.class))).thenReturn(saved);

        TraitDTO result = traitService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Dedication", result.getName());
        assertEquals(1L, result.getTeacherId());

        verify(teacherRepository).findById(1L);
        verify(traitRepository).existsByTeacher_IdAndNameIgnoreCase(1L, "Dedication");
        verify(traitRepository).save(any(Trait.class));
    }

    @Test
    void shouldUpdateTrait() {
        Teacher teacher = Teacher.builder().id(1L).build();

        Trait existing = Trait.builder()
                .id(1L)
                .name("Focus")
                .teacher(teacher)
                .traitEvaluations(new HashSet<>())
                .build();

        TraitDTO dto = TraitDTO.builder()
                .name("Discipline")
                .teacherId(1L)
                .build();

        when(traitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        // no duplicate for new combination
        when(traitRepository.existsByTeacher_IdAndNameIgnoreCase(1L, "Discipline")).thenReturn(false);
        when(traitRepository.save(any(Trait.class))).thenAnswer(inv -> inv.getArgument(0));

        TraitDTO updated = traitService.update(1L, dto);

        assertNotNull(updated);
        assertEquals("Discipline", updated.getName());
        assertEquals(1L, updated.getTeacherId());

        verify(traitRepository).findById(1L);
        verify(teacherRepository).findById(1L);
        verify(traitRepository).existsByTeacher_IdAndNameIgnoreCase(1L, "Discipline");
        verify(traitRepository).save(any(Trait.class));
    }

    @Test
    void shouldListByTeacher() {
        Teacher teacher = Teacher.builder().id(1L).build();

        Trait t1 = Trait.builder().id(1L).name("Strength").teacher(teacher).traitEvaluations(new HashSet<>()).build();
        Trait t2 = Trait.builder().id(2L).name("Agility").teacher(teacher).traitEvaluations(new HashSet<>()).build();

        when(traitRepository.findByTeacher_Id(1L)).thenReturn(List.of(t1, t2));

        List<TraitDTO> result = traitService.listByTeacher(1L);

        assertEquals(2, result.size());
        assertEquals("Strength", result.get(0).getName());
        assertEquals("Agility", result.get(1).getName());

        verify(traitRepository).findByTeacher_Id(1L);
    }

    @Test
    void shouldDeleteTrait() {
        Trait trait = Trait.builder().id(1L).name("Commitment").build();

        when(traitRepository.findById(1L)).thenReturn(Optional.of(trait));

        boolean deleted = traitService.delete(1L);

        assertTrue(deleted);
        verify(traitRepository).delete(trait);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentTrait() {
        when(traitRepository.findById(99L)).thenReturn(Optional.empty());

        boolean deleted = traitService.delete(99L);

        assertFalse(deleted);
        verify(traitRepository, never()).delete(any());
    }
}
