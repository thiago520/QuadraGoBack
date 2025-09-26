package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.TraitEvaluationDTO;
import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.models.TraitEvaluationId;
import com.quadrago.backend.services.TraitEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/trait-evaluations", produces = "application/json")
@RequiredArgsConstructor
public class TraitEvaluationController {

    private final TraitEvaluationService traitEvaluationService;

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public ResponseEntity<TraitEvaluationDTO> create(@RequestBody @Valid TraitEvaluationDTO dto) {
        log.info("Creating trait evaluation: studentId={}, traitId={}", dto.getStudentId(), dto.getTraitId());
        TraitEvaluation created = traitEvaluationService.create(dto);
        TraitEvaluationDTO response = new TraitEvaluationDTO(created);
        log.info("Trait evaluation created: studentId={}, traitId={}", response.getStudentId(), response.getTraitId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public ResponseEntity<List<TraitEvaluationDTO>> listByStudent(@PathVariable Long studentId) {
        log.debug("Listing trait evaluations by studentId={}", studentId);
        List<TraitEvaluationDTO> list = traitEvaluationService.listByStudent(studentId);
        return ResponseEntity.ok(list);
    }

    @PutMapping(value = "/{studentId}/{traitId}", consumes = "application/json")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public ResponseEntity<TraitEvaluationDTO> update(
            @PathVariable Long studentId,
            @PathVariable Long traitId,
            @RequestBody @Valid TraitEvaluationDTO dto
    ) {
        log.info("Updating trait evaluation: studentId={}, traitId={}", studentId, traitId);
        TraitEvaluationId id = new TraitEvaluationId(studentId, traitId);
        TraitEvaluationDTO updated = traitEvaluationService.update(id, dto);
        log.info("Trait evaluation updated: studentId={}, traitId={}", updated.getStudentId(), updated.getTraitId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{studentId}/{traitId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long traitId
    ) {
        log.warn("Deleting trait evaluation: studentId={}, traitId={}", studentId, traitId);
        TraitEvaluationId id = new TraitEvaluationId(studentId, traitId);
        traitEvaluationService.delete(id);
        log.info("Trait evaluation deleted: studentId={}, traitId={}", studentId, traitId);
        return ResponseEntity.noContent().build();
    }
}
