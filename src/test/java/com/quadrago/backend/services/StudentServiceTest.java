package com.quadrago.backend.services;

import com.quadrago.backend.dtos.StudentDTO;
import com.quadrago.backend.dtos.StudentResponseDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void shouldCreateStudent() {
        Set<Long> teacherIds = Set.of(1L);
        StudentDTO dto = new StudentDTO("Joao", "12345678901", "joao@email.com", "9999999999", teacherIds);

        // uniqueness validations
        when(studentRepository.existsByNationalId("12345678901")).thenReturn(false);
        when(studentRepository.existsByEmail("joao@email.com")).thenReturn(false);

        when(teacherRepository.findAllById(teacherIds)).thenReturn(new ArrayList<>());

        Student saved = Student.builder()
                .id(1L)
                .name(dto.getName())
                .nationalId(dto.getNationalId())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .teachers(new HashSet<>())
                .build();

        when(studentRepository.save(any())).thenReturn(saved);

        Student result = studentService.create(dto);

        assertNotNull(result);
        assertEquals("Joao", result.getName());
        assertEquals("12345678901", result.getNationalId());
    }

    @Test
    void shouldListStudents() {
        List<Student> students = List.of(
                Student.builder().id(1L).name("A").nationalId("11111111111").email("a@email.com").phone("9999999999").teachers(new HashSet<>()).build(),
                Student.builder().id(2L).name("B").nationalId("22222222222").email("b@email.com").phone("8888888888").teachers(new HashSet<>()).build()
        );

        when(studentRepository.findAll()).thenReturn(students);

        List<StudentResponseDTO> result = studentService.list();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
    }

    @Test
    void shouldFindStudentById() {
        Student student = Student.builder()
                .id(1L)
                .name("Joao")
                .nationalId("12388888888")
                .email("j@email.com")
                .phone("9999999999")
                .teachers(new HashSet<>())
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Optional<Student> result = studentService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Joao", result.get().getName());
    }

    @Test
    void shouldUpdateStudent() {
        Student existing = Student.builder()
                .id(1L)
                .name("Joao")
                .nationalId("12333333333")
                .email("j@email.com")
                .phone("9999999999")
                .teachers(new HashSet<>())
                .build();

        Set<Long> newTeacherIds = Set.of(2L);
        StudentDTO dto = new StudentDTO("Novo Nome", "99999999999", "novo@email.com", "8888888888", newTeacherIds);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(teacherRepository.findAllById(newTeacherIds)).thenReturn(new ArrayList<>());

        // uniqueness checks for changed fields
        when(studentRepository.existsByNationalId("99999999999")).thenReturn(false);
        when(studentRepository.existsByEmail("novo@email.com")).thenReturn(false);

        when(studentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<Student> result = studentService.update(1L, dto);

        assertTrue(result.isPresent());
        assertEquals("Novo Nome", result.get().getName());
        assertEquals("99999999999", result.get().getNationalId());
        assertEquals("8888888888", result.get().getPhone());
    }

    @Test
    void shouldDeleteStudent() {
        Student student = Student.builder()
                .id(1L)
                .name("Joao")
                .nationalId("12345678901")
                .email("j@email.com")
                .phone("9990000000")
                .teachers(new HashSet<>())
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        boolean result = studentService.delete(1L);

        assertTrue(result);
        verify(studentRepository).delete(student);
    }

    @Test
    void shouldCreateStudentWithTeachers() {
        StudentDTO dto = new StudentDTO("Ana", "12345678900", "ana@email.com", "11988887777", Set.of(1L, 2L));

        Teacher t1 = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .phone("11999999999")
                .nationalId("12345678901")
                .students(new HashSet<>())
                .build();

        Teacher t2 = Teacher.builder()
                .id(2L)
                .name("Roberto")
                .phone("11998789999")
                .nationalId("12348778901")
                .students(new HashSet<>())
                .build();

        // uniqueness validations
        when(studentRepository.existsByNationalId("12345678900")).thenReturn(false);
        when(studentRepository.existsByEmail("ana@email.com")).thenReturn(false);

        when(teacherRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(t1, t2));

        Student saved = Student.builder()
                .id(1L)
                .name(dto.getName())
                .nationalId(dto.getNationalId())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .teachers(Set.of(t1, t2))
                .build();

        when(studentRepository.save(any())).thenReturn(saved);

        Student result = studentService.create(dto);

        assertEquals("Ana", result.getName());
        assertEquals(2, result.getTeachers().size());
        verify(teacherRepository).findAllById(dto.getTeacherIds());
        verify(studentRepository).save(any(Student.class));
    }
}
