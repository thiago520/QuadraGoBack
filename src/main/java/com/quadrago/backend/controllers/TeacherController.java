package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.TeacherDTO;
import com.quadrago.backend.dtos.TeacherResponseDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.services.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<TeacherResponseDTO>> list() {
        List<TeacherResponseDTO> resp = service.list()
                .stream()
                .map(TeacherResponseDTO::of)
                .toList();
        return ResponseEntity.ok(resp);
    }

    /**
     * Cadastro público de professor (sem autenticação).
     * Requerido pela atualização de segurança.
     */
    @PostMapping
    public ResponseEntity<TeacherResponseDTO> create(@RequestBody @Valid TeacherDTO dto) {
        Teacher novo = service.create(dto);
        return ResponseEntity
                .created(URI.create("/teachers/" + novo.getId()))
                .body(TeacherResponseDTO.of(novo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherResponseDTO> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(TeacherResponseDTO::of)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<TeacherResponseDTO> update(@PathVariable Long id, @RequestBody @Valid TeacherDTO dto) {
        return service.update(id, dto)
                .map(TeacherResponseDTO::of)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // apenas ADMIN pode deletar
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deletado = service.delete(id);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
