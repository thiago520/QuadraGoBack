package com.quadrago.backend.services;

import com.quadrago.backend.dtos.ClassGroupDTO;
import com.quadrago.backend.dtos.ClassGroupResponseDTO;
import com.quadrago.backend.dtos.ClassScheduleDTO;
import com.quadrago.backend.enums.Level;
import com.quadrago.backend.models.ClassGroup;
import com.quadrago.backend.models.ClassSchedule;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.repositories.ClassGroupRepository;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
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
public class ClassGroupServiceTest { // se preferir: ClassGroupServiceTest

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ClassGroupService classGroupService;

    private Student createStudentWithScores(int... scores) {
        Student student = new Student();
        student.setId(Math.abs(UUID.randomUUID().getLeastSignificantBits()));
        Set<TraitEvaluation> evaluations = new HashSet<>();
        for (int score : scores) {
            TraitEvaluation e = new TraitEvaluation();
            e.setScore(score);               // campo refatorado (antes: nota)
            e.setStudent(student);
            e.setTrait(new Trait());         // necessário, mas irrelevante para a média
            evaluations.add(e);
        }
        student.setTraitEvaluations(evaluations);
        return student;
    }

    @Test
    void shouldSaveClassGroupSuccessfully() {
        ClassGroupDTO dto = ClassGroupDTO.builder()
                .name("ClassGroup A")
                .level(Level.INTERMEDIATE) // enum alinhado
                .teacherId(1L)
                .studentIds(Set.of(1L, 2L))
                .schedules(Set.of(new ClassScheduleDTO(
                        DayOfWeek.TUESDAY, LocalTime.of(10, 0), Duration.ofHours(1)
                )))
                .build();

        Teacher teacher = new Teacher();
        teacher.setId(1L);

        Student student1 = createStudentWithScores(4, 5);
        Student student2 = createStudentWithScores(6, 7);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(student1, student2));
        when(classGroupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassGroupResponseDTO response = classGroupService.salvar(dto); // service ainda com nome PT-BR

        assertEquals("ClassGroup A", response.getName());
        assertEquals(Level.INTERMEDIATE, response.getComputedLevel()); // campo refatorado
    }

    @Test
    void shouldUpdateClassGroup() {
        Long id = 1L;

        ClassGroup existing = new ClassGroup();
        existing.setId(id);
        existing.setName("Old");
        existing.setStudents(new HashSet<>());
        existing.setSchedules(new HashSet<>());

        ClassGroupDTO dto = ClassGroupDTO.builder()
                .name("New ClassGroup")
                .level(Level.ADVANCED)
                .teacherId(2L)
                .studentIds(Set.of(1L))
                .schedules(Set.of(new ClassScheduleDTO(
                        DayOfWeek.THURSDAY, LocalTime.of(14, 0), Duration.ofMinutes(90)
                )))
                .build();

        Teacher teacher = new Teacher();
        teacher.setId(2L);

        Student student = createStudentWithScores(9, 10);

        when(classGroupRepository.findById(id)).thenReturn(Optional.of(existing));
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(studentRepository.findAllById(Set.of(1L))).thenReturn(List.of(student));
        when(classGroupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassGroupResponseDTO updated = classGroupService.atualizar(id, dto); // service ainda com nome PT-BR

        assertEquals("New ClassGroup", updated.getName());
        assertEquals(Level.ADVANCED, updated.getComputedLevel());
    }

    @Test
    void shouldDeleteClassGroupSuccessfully() {
        ClassGroup group = new ClassGroup();
        group.setId(1L);

        when(classGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        boolean deleted = classGroupService.deletar(1L);

        assertTrue(deleted);
        verify(classGroupRepository).delete(group);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentClassGroup() {
        when(classGroupRepository.findById(99L)).thenReturn(Optional.empty());

        boolean deleted = classGroupService.deletar(99L);

        assertFalse(deleted);
    }

    @Test
    void shouldFindClassGroupByIdSuccessfully() {
        ClassGroup group = new ClassGroup();
        group.setId(1L);
        group.setName("ClassGroup X");
        group.setLevel(Level.BEGINNER); // enum em inglês
        group.setTeacher(new Teacher());
        group.setStudents(Set.of());
        group.setSchedules(Set.of());

        when(classGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        ClassGroupResponseDTO dto = classGroupService.buscarPorId(1L);

        assertEquals("ClassGroup X", dto.getName());
        assertEquals(Level.BEGINNER, dto.getLevel());
    }

    @Test
    void shouldThrowWhenClassGroupNotFound() {
        when(classGroupRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> classGroupService.buscarPorId(404L));
    }

    @Test
    void shouldReturnAllClassGroups() {
        ClassGroup t1 = new ClassGroup();
        t1.setId(1L);
        t1.setName("T1");
        t1.setTeacher(new Teacher());
        t1.setStudents(Set.of());
        t1.setSchedules(Set.of());

        ClassGroup t2 = new ClassGroup();
        t2.setId(2L);
        t2.setName("T2");
        t2.setTeacher(new Teacher());
        t2.setStudents(Set.of());
        t2.setSchedules(Set.of());

        when(classGroupRepository.findAll()).thenReturn(List.of(t1, t2));

        List<ClassGroupResponseDTO> result = classGroupService.listar();

        assertEquals(2, result.size());
        assertEquals("T1", result.get(0).getName());
        assertEquals("T2", result.get(1).getName());
    }

    @Test
    void shouldComputeAdvancedLevelForHighAverage() {
        Student s1 = createStudentWithScores(9, 10);
        Student s2 = createStudentWithScores(8, 9);

        ClassGroup group = new ClassGroup();
        group.setStudents(Set.of(s1, s2));

        when(classGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        Optional<Level> level = classGroupService.calcularNivelTurma(1L);

        assertTrue(level.isPresent());
        assertEquals(Level.ADVANCED, level.get());
    }

    @Test
    void shouldBeBeginnerWhenNoStudents() {
        ClassGroup group = new ClassGroup();
        group.setStudents(Set.of());

        when(classGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        Optional<Level> level = classGroupService.calcularNivelTurma(1L);

        assertTrue(level.isPresent());
        assertEquals(Level.BEGINNER, level.get());
    }
}
