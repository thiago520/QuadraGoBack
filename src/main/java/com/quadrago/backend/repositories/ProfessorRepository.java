package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
