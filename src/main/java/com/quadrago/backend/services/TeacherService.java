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
        validateNationalIdUniqueness(dto.getNationalId(), Optional.empty());

        Teacher t = Teacher.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .nationalId(dto.getNationalId())
                .build();

        Teacher saved = repository.save(t);
        log.info("Teacher saved: id={}, name='{}'", saved.getId(), saved.getName());
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
        return repository.findById(id).map(existing -> {
            if (!Objects.equals(existing.getNationalId(), dto.getNationalId())) {
                validateNationalIdUniqueness(dto.getNationalId(), Optional.of(id));
            }
            existing.setName(dto.getName());
            existing.setPhone(dto.getPhone());
            existing.setNationalId(dto.getNationalId());
            Teacher updated = repository.save(existing);
            log.info("Teacher updated: id={}, name='{}'", updated.getId(), updated.getName());
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

    private void validateNationalIdUniqueness(String nationalId, Optional<Long> selfId) {
        if (nationalId == null || nationalId.isBlank()) return;

        boolean exists = repository.existsByNationalId(nationalId);
        if (!exists) return;

        // if updating, allow same value for the same record
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
}
