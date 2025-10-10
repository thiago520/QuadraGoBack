package com.quadrago.backend.repositories;

import com.quadrago.backend.models.TeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    boolean existsByUserId(Long userId);
}