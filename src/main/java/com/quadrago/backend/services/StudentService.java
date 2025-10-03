package com.quadrago.backend.services;

import com.quadrago.backend.dtos.StudentDTO;
import com.quadrago.backend.dtos.StudentResponseDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    /* ===================== CREATE ===================== */

    @Transactional
    public Student create(StudentDTO dto) {
        // Normalização
        String name       = safeTrim(dto.getName());
        String email      = normalizeEmail(dto.getEmail());
        String phone      = normalizeDigits(dto.getPhone());
        String nationalId = normalizeDigits(dto.getNationalId());

        // Regras de unicidade
        validateUniqueness(nationalId, email, Optional.empty());

        // Hash da senha (obrigatória no DTO)
        String passwordHash = passwordEncoder.encode(dto.getPassword());

        Student student = Student.builder()
                .name(name)
                .email(email)
                .password(passwordHash)
                .nationalId(nationalId)
                .phone(phone)
                .teachers(resolveTeachers(dto))
                .build();

        Student saved = studentRepository.save(student);
        log.info("Student saved: id={}, email='{}'", saved.getId(), saved.getEmail());
        return saved;
    }

    /* ===================== READ ===================== */

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> list() {
        log.debug("Listing students");
        return studentRepository.findAll().stream()
                .map(StudentResponseDTO::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public Optional<Student> update(Long id, StudentDTO dto) {
        // Normaliza entradas
        String newName       = safeTrim(dto.getName());
        String newEmail      = normalizeEmail(dto.getEmail());
        String newPhone      = normalizeDigits(dto.getPhone());
        String newNationalId = normalizeDigits(dto.getNationalId());
        String newPassword   = dto.getPassword();

        return studentRepository.findById(id).map(existing -> {
            // valida unicidade apenas se mudou
            boolean changedNationalId = !Objects.equals(existing.getNationalId(), newNationalId);
            boolean changedEmail      = !Objects.equals(normalizeEmail(existing.getEmail()), newEmail);
            if (changedNationalId || changedEmail) {
                validateUniqueness(newNationalId, newEmail, Optional.of(id));
            }

            existing.setName(newName);
            existing.setEmail(newEmail);
            existing.setPhone(newPhone);
            existing.setNationalId(newNationalId);
            existing.setTeachers(resolveTeachers(dto));

            if (newPassword != null && !newPassword.isBlank()) {
                existing.setPassword(passwordEncoder.encode(newPassword));
            }

            Student updated = studentRepository.save(existing);
            log.info("Student updated: id={}, email='{}'", updated.getId(), updated.getEmail());
            return updated;
        });
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public boolean delete(Long id) {
        return studentRepository.findById(id).map(st -> {
            log.warn("Deleting student id={}", id);
            studentRepository.delete(st);
            log.info("Student deleted id={}", id);
            return true;
        }).orElse(false);
    }

    /* ===================== Helpers ===================== */

    private Set<Teacher> resolveTeachers(StudentDTO dto) {
        if (dto.getTeacherIds() == null || dto.getTeacherIds().isEmpty()) return new HashSet<>();
        return new HashSet<>(teacherRepository.findAllById(dto.getTeacherIds()));
    }

    private void validateUniqueness(String nationalId, String email, Optional<Long> selfId) {
        // nationalId (CPF) — somente dígitos
        if (nationalId != null && !nationalId.isBlank()) {
            boolean existsCpf = studentRepository.existsByNationalId(nationalId);
            if (existsCpf) {
                if (selfId.isPresent()) {
                    Long id = selfId.get();
                    String current = studentRepository.findById(id)
                            .map(Student::getNationalId)
                            .orElse(null);
                    if (Objects.equals(current, nationalId)) {
                        // é o mesmo registro — ok
                    } else {
                        throw new IllegalArgumentException("nationalId already in use");
                    }
                } else {
                    throw new IllegalArgumentException("nationalId already in use");
                }
            }
        }

        // email — lower-case / trim
        if (email != null && !email.isBlank()) {
            boolean existsEmail = studentRepository.existsByEmail(email);
            if (existsEmail) {
                if (selfId.isPresent()) {
                    Long id = selfId.get();
                    String current = normalizeEmail(studentRepository.findById(id)
                            .map(Student::getEmail)
                            .orElse(null));
                    if (Objects.equals(current, email)) {
                        // é o mesmo registro — ok
                    } else {
                        throw new IllegalArgumentException("email already in use");
                    }
                } else {
                    throw new IllegalArgumentException("email already in use");
                }
            }
        }
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
    private static String normalizeEmail(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }
    /** Mantém apenas dígitos (para phone e nacionalId/CPF) */
    private static String normalizeDigits(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
