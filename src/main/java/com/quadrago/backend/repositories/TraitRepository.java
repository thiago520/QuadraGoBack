package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Trait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    /** All traits owned by a given teacher */
    List<Trait> findByTeacher_Id(Long teacherId);

    /** Useful for validation: prevent duplicated names per teacher (case-insensitive) */
    boolean existsByTeacher_IdAndNameIgnoreCase(Long teacherId, String name);

    Optional<Trait> findByTeacher_IdAndNameIgnoreCase(Long teacherId, String name);
}
