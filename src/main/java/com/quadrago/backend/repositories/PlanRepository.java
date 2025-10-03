package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByTeacherIdAndActiveTrueOrderByTitleAsc(Long teacherId);
    Optional<Plan> findByIdAndTeacherId(Long id, Long teacherId);
    boolean existsByTeacherIdAndTitleIgnoreCase(Long teacherId, String title);
    boolean existsByTeacherIdAndTitleIgnoreCaseAndIdNot(Long teacherId, String title, Long id);
}
