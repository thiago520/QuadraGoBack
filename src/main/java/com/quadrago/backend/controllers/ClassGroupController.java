package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.ClassGroupDTO;
import com.quadrago.backend.dtos.ClassGroupResponseDTO;
import com.quadrago.backend.enums.Level;
import com.quadrago.backend.services.ClassGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/class-groups", produces = "application/json")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ClassGroupResponseDTO> create(@RequestBody @Valid ClassGroupDTO dto) {
        log.info("Creating class group: name='{}'", dto.getName());
        ClassGroupResponseDTO created = classGroupService.salvar(dto); // service ainda em PT-BR, ok
        log.info("Class group created: id={}, name='{}'", created.getId(), created.getName());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<ClassGroupResponseDTO>> list() {
        log.debug("Listing class groups");
        return ResponseEntity.ok(classGroupService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ClassGroupResponseDTO> findById(@PathVariable Long id) {
        log.debug("Fetching class group by id={}", id);
        return ResponseEntity.ok(classGroupService.buscarPorId(id));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ClassGroupResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ClassGroupDTO dto) {
        log.info("Updating class group id={}", id);
        ClassGroupResponseDTO updated = classGroupService.atualizar(id, dto);
        log.info("Class group updated id={}, name='{}'", updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("Deleting class group id={}", id);
        boolean deleted = classGroupService.deletar(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/level")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Level> calculateLevel(@PathVariable Long id) {
        log.debug("Calculating class group level id={}", id);
        return classGroupService.calcularNivelTurma(id)
                .map(level -> {
                    log.info("Calculated level for class group id={}: {}", id, level);
                    return ResponseEntity.ok(level);
                })
                .orElseGet(() -> {
                    log.warn("Class group not found for level calculation id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
