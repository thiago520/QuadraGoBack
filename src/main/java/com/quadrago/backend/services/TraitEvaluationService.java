package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TraitEvaluationDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.models.TraitEvaluationId;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TraitEvaluationRepository;
import com.quadrago.backend.repositories.TraitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraitEvaluationService {

    private final TraitEvaluationRepository evaluationRepository;
    private final TraitRepository traitRepository;
    private final StudentRepository studentRepository;

    /* ===================== CREATE ===================== */

    @Transactional
    public TraitEvaluation create(TraitEvaluationDTO dto) {
        Long studentId = dto.getStudentId();
        Long traitId   = dto.getTraitId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found: id=" + studentId));

        Trait trait = traitRepository.findById(traitId)
                .orElseThrow(() -> new NoSuchElementException("Trait not found: id=" + traitId));

        // Evita duplicidade (um par student/trait deve ser único)
        if (evaluationRepository.existsByStudent_IdAndTrait_Id(studentId, traitId)) {
            log.warn("Evaluation already exists for studentId={}, traitId={}", studentId, traitId);
            throw new IllegalArgumentException("evaluation already exists for this student and trait");
        }

        TraitEvaluationId id = new TraitEvaluationId(studentId, traitId);

        TraitEvaluation evaluation = TraitEvaluation.builder()
                .id(id)
                .student(student)
                .trait(trait)
                .score(dto.getScore())
                .build();

        TraitEvaluation saved = evaluationRepository.save(evaluation);
        log.info("TraitEvaluation saved: studentId={}, traitId={}, score={}", studentId, traitId, saved.getScore());
        return saved;
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public TraitEvaluationDTO update(TraitEvaluationId id, TraitEvaluationDTO dto) {
        TraitEvaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TraitEvaluation not found: " + id));

        Long newStudentId = dto.getStudentId();
        Long newTraitId   = dto.getTraitId();

        // Reassociar se IDs mudarem
        if (newStudentId != null && !newStudentId.equals(evaluation.getStudent().getId())) {
            Student student = studentRepository.findById(newStudentId)
                    .orElseThrow(() -> new EntityNotFoundException("Student not found: id=" + newStudentId));
            evaluation.setStudent(student);
        }
        if (newTraitId != null && !newTraitId.equals(evaluation.getTrait().getId())) {
            Trait trait = traitRepository.findById(newTraitId)
                    .orElseThrow(() -> new EntityNotFoundException("Trait not found: id=" + newTraitId));
            evaluation.setTrait(trait);
        }

        evaluation.setScore(dto.getScore());
        TraitEvaluation updated = evaluationRepository.save(evaluation);
        log.info("TraitEvaluation updated: studentId={}, traitId={}, score={}",
                updated.getStudent().getId(), updated.getTrait().getId(), updated.getScore());

        return new TraitEvaluationDTO(updated);
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public boolean delete(TraitEvaluationId id) {
        return evaluationRepository.findById(id).map(ev -> {
            log.warn("Deleting TraitEvaluation: studentId={}, traitId={}", id.getStudentId(), id.getTraitId());
            evaluationRepository.delete(ev);
            log.info("TraitEvaluation deleted: studentId={}, traitId={}", id.getStudentId(), id.getTraitId());
            return true;
        }).orElse(false);
    }

    /* ===================== READ ===================== */

    @Transactional(readOnly = true)
    public List<TraitEvaluationDTO> listByStudent(Long studentId) {
        List<TraitEvaluationDTO> list = evaluationRepository.findByStudent_Id(studentId).stream()
                .map(TraitEvaluationDTO::new)
                .toList();
        log.debug("Listed {} evaluations for studentId={}", list.size(), studentId);
        return list;
    }

    @Transactional(readOnly = true)
    public List<TraitEvaluationDTO> listByTrait(Long traitId) {
        List<TraitEvaluationDTO> list = evaluationRepository.findByTrait_Id(traitId).stream()
                .map(TraitEvaluationDTO::new)
                .toList();
        log.debug("Listed {} evaluations for traitId={}", list.size(), traitId);
        return list;
    }
}
