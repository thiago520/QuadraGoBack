package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
}
