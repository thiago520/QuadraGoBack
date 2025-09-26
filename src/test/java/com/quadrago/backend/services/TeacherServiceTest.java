package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TeacherDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

    @Mock
    private TeacherRepository repository;

    @InjectMocks
    private TeacherService service;

    @Test
    void shouldCreateTeacher() {
        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos")
                .phone("11999999999")
                .nationalId("12345678901")
                .build();

        // uniqueness validation
        when(repository.existsByNationalId("12345678901")).thenReturn(false);

        Teacher saved = Teacher.builder()
                .id(1L)
                .name(dto.getName())
                .phone(dto.getPhone())
                .nationalId(dto.getNationalId())
                .build();

        when(repository.save(any(Teacher.class))).thenReturn(saved);

        Teacher result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Carlos");

        verify(repository).existsByNationalId("12345678901");
        verify(repository).save(any(Teacher.class));
    }

    @Test
    void shouldListTeachers() {
        Teacher t1 = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .phone("111")
                .nationalId("123")
                .build();

        Teacher t2 = Teacher.builder()
                .id(2L)
                .name("Ana")
                .phone("222")
                .nationalId("456")
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Teacher> list = service.list();

        assertThat(list).hasSize(2);
        assertThat(list).extracting(Teacher::getName).containsExactly("Carlos", "Ana");

        verify(repository).findAll();
    }

    @Test
    void shouldFindById() {
        Teacher t = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .phone("111")
                .nationalId("123")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(t));

        Optional<Teacher> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo("Carlos");

        verify(repository).findById(1L);
    }

    @Test
    void shouldUpdateTeacher() {
        Teacher existing = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .phone("111")
                .nationalId("123")
                .build();

        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos Updated")
                .phone("999")
                .nationalId("789")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        // uniqueness validation for changed nationalId
        when(repository.existsByNationalId("789")).thenReturn(false);
        when(repository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Teacher> result = service.update(1L, dto);

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo("Carlos Updated");
        assertThat(result.get().getPhone()).isEqualTo("999");
        assertThat(result.get().getNationalId()).isEqualTo("789");

        verify(repository).findById(1L);
        verify(repository).existsByNationalId("789");
        verify(repository).save(any(Teacher.class));
    }

    @Test
    void shouldDeleteById() {
        Long id = 1L;

        Teacher teacher = Teacher.builder()
                .id(id)
                .name("Name")
                .phone("123")
                .nationalId("000")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(teacher));
        doNothing().when(repository).delete(teacher);

        boolean result = service.delete(id);

        assertTrue(result);

        verify(repository).findById(id);
        verify(repository).delete(teacher);
    }
}
