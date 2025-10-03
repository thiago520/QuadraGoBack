package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.PlanDTO;
import com.quadrago.backend.dtos.PlanResponseDTO;
import com.quadrago.backend.services.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/teachers/{teacherId}/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /** Público: lista os planos ativos de um professor */
    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> list(@PathVariable Long teacherId) {
        return ResponseEntity.ok(planService.listPublicByTeacher(teacherId));
    }

    /** Protegido: criar plano (TEACHER/ADMIN). */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<PlanResponseDTO> create(@PathVariable Long teacherId, @RequestBody @Valid PlanDTO dto) {
        PlanResponseDTO created = planService.create(teacherId, dto);
        return ResponseEntity.created(URI.create("/teachers/" + teacherId + "/plans/" + created.getId()))
                .body(created);
    }

    /** Protegido: atualizar plano */
    @PutMapping("/{planId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<PlanResponseDTO> update(@PathVariable Long teacherId, @PathVariable Long planId,
                                                  @RequestBody @Valid PlanDTO dto) {
        return ResponseEntity.ok(planService.update(teacherId, planId, dto));
    }

    /** Protegido: deletar plano */
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long teacherId, @PathVariable Long planId) {
        planService.delete(teacherId, planId);
        return ResponseEntity.noContent().build();
    }
}
