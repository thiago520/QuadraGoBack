package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.TraitDTO;
import com.quadrago.backend.services.TraitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/traits", produces = "application/json")
@RequiredArgsConstructor
public class TraitController {

    private final TraitService traitService;

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TraitDTO> create(@RequestBody @Valid TraitDTO dto) {
        log.info("Creating trait: name='{}', teacherId={}", dto.getName(), dto.getTeacherId());
        TraitDTO created = traitService.create(dto);
        log.info("Trait created: id={}, name='{}'", created.getId(), created.getName());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<List<TraitDTO>> listByTeacher(@PathVariable Long teacherId) {
        log.debug("Listing traits by teacherId={}", teacherId);
        List<TraitDTO> list = traitService.listByTeacher(teacherId); // service ainda em PT-BR
        return ResponseEntity.ok(list);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TraitDTO> update(@PathVariable Long id, @RequestBody @Valid TraitDTO dto) {
        log.info("Updating trait id={}", id);
        TraitDTO updated = traitService.update(id, dto);
        log.info("Trait updated id={}, name='{}'", updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("Deleting trait id={}", id);
        traitService.delete(id);
        log.info("Trait deleted id={}", id);
        return ResponseEntity.noContent().build();
    }
}
