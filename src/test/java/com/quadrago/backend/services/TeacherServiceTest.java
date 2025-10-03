package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TeacherDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

    @Mock
    private TeacherRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService service;

    @Test
    void shouldCreateTeacher() {
        // dado um DTO com email em caixa alta e phone/cpf "formatados"
        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos")
                .email("CARLOS.SILVA@EXEMPLO.COM")
                .password("SenhaForte123")
                .phone("(11) 99999-9999")
                .nationalId("123.456.789-01")
                .build();

        // unicidade
        when(repository.existsByEmail("carlos.silva@exemplo.com")).thenReturn(false);
        when(repository.existsByNationalId("12345678901")).thenReturn(false);

        // hash da senha
        when(passwordEncoder.encode("SenhaForte123")).thenReturn("hash");

        Teacher saved = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .email("carlos.silva@exemplo.com")
                .password("hash")
                .phone("11999999999")
                .nationalId("12345678901")
                .build();

        when(repository.save(any(Teacher.class))).thenReturn(saved);

        Teacher result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Carlos");
        assertThat(result.getEmail()).isEqualTo("carlos.silva@exemplo.com"); // normalizado
        assertThat(result.getPhone()).isEqualTo("11999999999");               // apenas dígitos
        assertThat(result.getNationalId()).isEqualTo("12345678901");          // apenas dígitos

        verify(repository).existsByEmail("carlos.silva@exemplo.com");
        verify(repository).existsByNationalId("12345678901");
        verify(passwordEncoder).encode("SenhaForte123");
        verify(repository).save(any(Teacher.class));
    }

    @Test
    void shouldFailCreateWhenEmailAlreadyExists() {
        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("SenhaForte123")
                .phone("11999999999")
                .nationalId("12345678901")
                .build();

        when(repository.existsByEmail("carlos@exemplo.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertEquals("email already in use", ex.getMessage());

        verify(repository).existsByEmail("carlos@exemplo.com");
        verify(repository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldFailCreateWhenNationalIdAlreadyExists() {
        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("SenhaForte123")
                .phone("11999999999")
                .nationalId("12345678901")
                .build();

        when(repository.existsByEmail("carlos@exemplo.com")).thenReturn(false);
        when(repository.existsByNationalId("12345678901")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        assertEquals("nationalId already in use", ex.getMessage());

        verify(repository).existsByEmail("carlos@exemplo.com");
        verify(repository).existsByNationalId("12345678901");
        verify(repository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldListTeachers() {
        Teacher t1 = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("hash")
                .phone("111")
                .nationalId("123")
                .build();

        Teacher t2 = Teacher.builder()
                .id(2L)
                .name("Ana")
                .email("ana@exemplo.com")
                .password("hash")
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
                .email("carlos@exemplo.com")
                .password("hash")
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
    void shouldUpdateTeacher_WhenEmailAndNationalIdChange() {
        Teacher existing = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("hash")
                .phone("111")
                .nationalId("12345678901")
                .build();

        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos Updated")
                .email("CARLOS.UPDATED@EXEMPLO.COM")
                .password("NovaSenha123") // opcional; se vier, service atualiza e hasheia
                .phone("11 97777-6666")
                .nationalId("999.888.777-66")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("carlos.updated@exemplo.com")).thenReturn(false);
        when(repository.existsByNationalId("99988877766")).thenReturn(false);
        when(repository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // hash da nova senha
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("new-hash");

        Optional<Teacher> result = service.update(1L, dto);

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo("Carlos Updated");
        assertThat(result.get().getEmail()).isEqualTo("carlos.updated@exemplo.com"); // normalizado
        assertThat(result.get().getPhone()).isEqualTo("11977776666");
        assertThat(result.get().getNationalId()).isEqualTo("99988877766");

        verify(repository, atLeastOnce()).findById(1L);
        verify(repository).existsByEmail("carlos.updated@exemplo.com");
        verify(repository).existsByNationalId("99988877766");
        verify(passwordEncoder).encode("NovaSenha123");
        verify(repository).save(any(Teacher.class));
    }

    @Test
    void shouldUpdateTeacher_WhenEmailUnchanged_DoesNotCheckEmailUniqueness() {
        Teacher existing = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("hash")
                .phone("111")
                .nationalId("12345678901")
                .build();

        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos Updated")
                .email("carlos@exemplo.com") // mesmo email
                .phone("11 97777-6666")
                .nationalId("12345678901") // mesmo cpf
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Teacher> result = service.update(1L, dto);

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo("Carlos Updated");
        assertThat(result.get().getEmail()).isEqualTo("carlos@exemplo.com"); // permanece
        assertThat(result.get().getPhone()).isEqualTo("11977776666");        // normalizado
        assertThat(result.get().getNationalId()).isEqualTo("12345678901");   // permanece

        verify(repository).findById(1L);
        // não deve chamar existsByEmail/existsByNationalId
        verify(repository, never()).existsByEmail(anyString());
        verify(repository, never()).existsByNationalId(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository).save(any(Teacher.class));
    }

    @Test
    void shouldFailUpdate_WhenChangingToExistingEmail() {
        Teacher existing = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("hash")
                .phone("111")
                .nationalId("12345678901")
                .build();

        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos")
                .email("duplicado@exemplo.com")
                .phone("111")
                .nationalId("12345678901")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("duplicado@exemplo.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(1L, dto));
        assertEquals("email already in use", ex.getMessage());

        verify(repository, atLeastOnce()).findById(1L);
        verify(repository).existsByEmail("duplicado@exemplo.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailUpdate_WhenChangingToExistingNationalId() {
        Teacher existing = Teacher.builder()
                .id(1L)
                .name("Carlos")
                .email("carlos@exemplo.com")
                .password("hash")
                .phone("111")
                .nationalId("12345678901")
                .build();

        TeacherDTO dto = TeacherDTO.builder()
                .name("Carlos")
                .email("carlos@exemplo.com")
                .phone("111")
                .nationalId("11122233344")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByNationalId("11122233344")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(1L, dto));
        assertEquals("nationalId already in use", ex.getMessage());

        // pode ser chamado mais de uma vez por causa da validação de unicidade
        verify(repository, atLeastOnce()).findById(1L);
        verify(repository).existsByNationalId("11122233344");
        verify(passwordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteById() {
        Long id = 1L;

        Teacher teacher = Teacher.builder()
                .id(id)
                .name("Name")
                .email("name@exemplo.com")
                .password("hash")
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
