package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.TurmaDTO;
import com.quadrago.backend.dtos.TurmaResponseDTO;
import com.quadrago.backend.enums.Nivel;
import com.quadrago.backend.services.TurmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TurmaResponseDTO> criar(@RequestBody @Valid TurmaDTO dto) {
        TurmaResponseDTO turmaCriada = turmaService.salvar(dto);
        return ResponseEntity.status(201).body(turmaCriada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<TurmaResponseDTO>> listar() {
        return ResponseEntity.ok(turmaService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TurmaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(turmaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<TurmaResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid TurmaDTO dto) {
        TurmaResponseDTO atualizada = turmaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        boolean deletado = turmaService.deletar(id);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/classificacao")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Nivel> calcularClassificacao(@PathVariable Long id) {
        return turmaService.calcularNivelTurma(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
