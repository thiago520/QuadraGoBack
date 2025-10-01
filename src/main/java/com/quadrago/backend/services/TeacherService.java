package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TeacherDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository repository;

    @Transactional
    public Teacher create(TeacherDTO dto) {
        // Normaliza entradas
        String name       = safeTrim(dto.getName());
        String email      = normalizeEmail(dto.getEmail());
        String phone      = normalizeDigits(dto.getPhone());
        String nationalId = normalizeDigits(dto.getNationalId());

        // Regras de unicidade
        validateEmailUniqueness(email, Optional.empty());
        validateNationalIdUniqueness(nationalId, Optional.empty());

        Teacher t = Teacher.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .nationalId(nationalId)
                .build();

        Teacher saved = repository.save(t);
        log.info("Teacher saved: id={}, name='{}', email='{}'", saved.getId(), saved.getName(), saved.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public java.util.List<Teacher> list() {
        log.debug("Listing teachers");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Optional<Teacher> update(Long id, TeacherDTO dto) {
        // Normaliza entradas antes de comparar/persistir
        String newName       = safeTrim(dto.getName());
        String newEmail      = normalizeEmail(dto.getEmail());
        String newPhone      = normalizeDigits(dto.getPhone());
        String newNationalId = normalizeDigits(dto.getNationalId());

        return repository.findById(id).map(existing -> {
            // Se e-mail mudou, valida unicidade
            if (!Objects.equals(existing.getEmail(), newEmail)) {
                validateEmailUniqueness(newEmail, Optional.of(id));
            }
            // Se nationalId mudou, valida unicidade
            if (!Objects.equals(existing.getNationalId(), newNationalId)) {
                validateNationalIdUniqueness(newNationalId, Optional.of(id));
            }

            existing.setName(newName);
            existing.setEmail(newEmail);
            existing.setPhone(newPhone);
            existing.setNationalId(newNationalId);

            Teacher updated = repository.save(existing);
            log.info("Teacher updated: id={}, name='{}', email='{}'", updated.getId(), updated.getName(), updated.getEmail());
            return updated;
        });
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.findById(id).map(teacher -> {
            log.warn("Deleting teacher id={}", id);
            repository.delete(teacher);
            log.info("Teacher deleted id={}", id);
            return true;
        }).orElse(false);
    }

    /* ------------ helpers ------------ */

    private void validateEmailUniqueness(String email, Optional<Long> selfId) {
        if (email == null || email.isBlank()) return;

        boolean exists = repository.existsByEmail(email);
        if (!exists) return;

        if (selfId.isPresent()) {
            Long id = selfId.get();
            String current = repository.findById(id)
                    .map(Teacher::getEmail)
                    .orElse(null);
            if (Objects.equals(current, email)) return;
        }

        log.warn("email already in use: {}", email);
        throw new IllegalArgumentException("email already in use");
    }

    private void validateNationalIdUniqueness(String nationalId, Optional<Long> selfId) {
        if (nationalId == null || nationalId.isBlank()) return;

        boolean exists = repository.existsByNationalId(nationalId);
        if (!exists) return;

        if (selfId.isPresent()) {
            Long id = selfId.get();
            String current = repository.findById(id)
                    .map(Teacher::getNationalId)
                    .orElse(null);
            if (Objects.equals(current, nationalId)) return;
        }

        log.warn("nationalId already in use: {}", nationalId);
        throw new IllegalArgumentException("nationalId already in use");
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private static String normalizeEmail(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    /** Mantém somente dígitos (para phone e nationalId/CPF) */
    private static String normalizeDigits(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
