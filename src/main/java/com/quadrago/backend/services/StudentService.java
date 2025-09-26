package com.quadrago.backend.services;

import com.quadrago.backend.dtos.StudentDTO;
import com.quadrago.backend.dtos.StudentResponseDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /* ===================== CREATE ===================== */

    @Transactional
    public Student create(StudentDTO dto) {
        validateUniqueness(dto.getNationalId(), dto.getEmail());

        Student student = Student.builder()
                .name(dto.getName())
                .nationalId(dto.getNationalId())
                .email(dto.getEmail())
                .phone(dto.getPhone())
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
                .map(StudentResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public Optional<Student> update(Long id, StudentDTO dto) {
        return studentRepository.findById(id).map(existing -> {
            if (!Objects.equals(existing.getNationalId(), dto.getNationalId())
                    || !Objects.equals(normalize(existing.getEmail()), normalize(dto.getEmail()))) {
                validateUniqueness(dto.getNationalId(), dto.getEmail(), Optional.of(id));
            }

            existing.setName(dto.getName());
            existing.setNationalId(dto.getNationalId());
            existing.setEmail(dto.getEmail());
            existing.setPhone(dto.getPhone());
            existing.setTeachers(resolveTeachers(dto));

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

    private void validateUniqueness(String nationalId, String email) {
        validateUniqueness(nationalId, email, Optional.empty());
    }

    private void validateUniqueness(String nationalId, String email, Optional<Long> selfId) {
        if (nationalId != null) {
            boolean existsNationalId = studentRepository.existsByNationalId(nationalId);
            if (existsNationalId && selfId.map(id -> !Objects.equals(
                    studentRepository.findById(id).map(Student::getNationalId).orElse(null),
                    nationalId)).orElse(true)) {
                throw new IllegalArgumentException("nationalId already in use");
            }
        }
        if (email != null && !email.isBlank()) {
            boolean existsEmail = studentRepository.existsByEmail(email);
            if (existsEmail && selfId.map(id -> !Objects.equals(
                    normalize(studentRepository.findById(id).map(Student::getEmail).orElse(null)),
                    normalize(email))).orElse(true)) {
                throw new IllegalArgumentException("email already in use");
            }
        }
    }

    private String normalize(String v) {
        return v == null ? null : v.trim().toLowerCase();
    }
}
