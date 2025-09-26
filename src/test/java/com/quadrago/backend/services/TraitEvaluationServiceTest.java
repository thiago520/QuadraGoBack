package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TraitEvaluationDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.models.TraitEvaluationId;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TraitEvaluationRepository;
import com.quadrago.backend.repositories.TraitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraitEvaluationServiceTest {

    @Mock
    private TraitEvaluationRepository evaluationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TraitRepository traitRepository;

    @InjectMocks
    private TraitEvaluationService evaluationService;

    private Student student;
    private Trait trait;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .name("João")
                .nationalId("12345678900")
                .email("joao@email.com")
                .phone("11999999999")
                .teachers(new HashSet<>())
                .traitEvaluations(new HashSet<>())
                .build();

        Teacher teacher = Teacher.builder()
                .id(1L)
                .name("Prof. Teste")
                .phone("11988887777")
                .nationalId("09876543210")
                .build();

        trait = Trait.builder()
                .id(2L)
                .name("Endurance")
                .teacher(teacher)
                .build();
    }

    @Test
    void shouldCreateEvaluation() {
        TraitEvaluationDTO dto = new TraitEvaluationDTO();
        dto.setStudentId(student.getId());
        dto.setTraitId(trait.getId());
        dto.setScore(7);

        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(traitRepository.findById(trait.getId())).thenReturn(Optional.of(trait));
        when(evaluationRepository.existsByStudent_IdAndTrait_Id(student.getId(), trait.getId())).thenReturn(false);

        TraitEvaluationId id = new TraitEvaluationId(student.getId(), trait.getId());

        TraitEvaluation saved = TraitEvaluation.builder()
                .id(id)
                .student(student)
                .trait(trait)
                .score(dto.getScore())
                .build();

        when(evaluationRepository.save(any())).thenReturn(saved);

        TraitEvaluation result = evaluationService.create(dto);

        assertNotNull(result);
        assertEquals(7, result.getScore());
        assertEquals(student.getId(), result.getStudent().getId());
        assertEquals(trait.getId(), result.getTrait().getId());
    }

    @Test
    void shouldUpdateScore() {
        TraitEvaluationDTO dto = new TraitEvaluationDTO();
        dto.setStudentId(student.getId());
        dto.setTraitId(trait.getId());
        dto.setScore(9);

        TraitEvaluationId id = new TraitEvaluationId(student.getId(), trait.getId());

        TraitEvaluation existing = TraitEvaluation.builder()
                .id(id)
                .student(student)
                .trait(trait)
                .score(5)
                .build();

        when(evaluationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(evaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TraitEvaluationDTO updated = evaluationService.update(id, dto);

        assertNotNull(updated);
        assertEquals(9, updated.getScore());
        assertEquals(student.getId(), updated.getStudentId());
        assertEquals(trait.getId(), updated.getTraitId());
    }

    @Test
    void shouldDeleteEvaluation() {
        TraitEvaluationId id = new TraitEvaluationId(1L, 2L);

        TraitEvaluation existing = TraitEvaluation.builder()
                .id(id)
                .student(student)
                .trait(trait)
                .score(8)
                .build();

        when(evaluationRepository.findById(id)).thenReturn(Optional.of(existing));

        boolean result = evaluationService.delete(id);

        assertTrue(result);
        verify(evaluationRepository, times(1)).delete(existing);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentEvaluation() {
        TraitEvaluationId id = new TraitEvaluationId(1L, 2L);
        when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

        boolean result = evaluationService.delete(id);

        assertFalse(result);
        verify(evaluationRepository, never()).delete(any());
    }
}
