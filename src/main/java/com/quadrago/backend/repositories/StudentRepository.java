package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    Optional<Student> findByEmail(String email);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.teachers")
    List<Student> findAllWithTeachers();
}
