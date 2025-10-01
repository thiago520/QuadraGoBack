package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.TeacherDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.services.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<Teacher>> list() {
        List<Teacher> professores = service.list();
        return ResponseEntity.ok(professores);
    }

    /**
     * Cadastro público de professor (sem autenticação).
     * Requerido pela atualização de segurança.
     */
    @PostMapping
    public ResponseEntity<Teacher> create(@RequestBody @Valid TeacherDTO dto) {
        Teacher novo = service.create(dto);
        // Opcional: retornar Location do recurso criado
        return ResponseEntity
                .created(URI.create("/teachers/" + novo.getId()))
                .body(novo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Teacher> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Teacher> update(@PathVariable Long id, @RequestBody @Valid TeacherDTO dto) {
        return service.update(id, dto)
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
