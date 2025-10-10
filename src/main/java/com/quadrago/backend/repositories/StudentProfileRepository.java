package com.quadrago.backend.repositories;

import com.quadrago.backend.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    boolean existsByUserId(Long userId);
}