package com.quadrago.backend.repositories;

import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.models.TraitEvaluationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraitEvaluationRepository extends JpaRepository<TraitEvaluation, TraitEvaluationId> {

    /* By trait */
    List<TraitEvaluation> findByTrait_Id(Long traitId);
    long countByTrait_Id(Long traitId);

    /* By student */
    List<TraitEvaluation> findByStudent_Id(Long studentId);
    Optional<TraitEvaluation> findByStudent_IdAndTrait_Id(Long studentId, Long traitId);
    boolean existsByStudent_IdAndTrait_Id(Long studentId, Long traitId);
    long deleteByStudent_IdAndTrait_Id(Long studentId, Long traitId);

    /* By teacher (owner of the trait) */
    List<TraitEvaluation> findByTrait_Teacher_Id(Long teacherId);

    /* If you prefer to query via embedded id (equivalentes): */
    // List<TraitEvaluation> findById_TraitId(Long traitId);
    // List<TraitEvaluation> findById_StudentId(Long studentId);
}
