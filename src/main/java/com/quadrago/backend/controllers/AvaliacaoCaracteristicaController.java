package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.AvaliacaoCaracteristicaDTO;
import com.quadrago.backend.models.AvaliacaoCaracteristica;
import com.quadrago.backend.services.AvaliacaoCaracteristicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoCaracteristicaController {

    private final AvaliacaoCaracteristicaService avaliacaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ALUNO')")
    public ResponseEntity<AvaliacaoCaracteristicaDTO> salvar(@RequestBody @Valid AvaliacaoCaracteristicaDTO dto) {
        AvaliacaoCaracteristica nova = avaliacaoService.salvar(dto);
        return ResponseEntity.status(201).body(new AvaliacaoCaracteristicaDTO(nova));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ALUNO')")
    public ResponseEntity<AvaliacaoCaracteristicaDTO> atualizar(@RequestBody @Valid AvaliacaoCaracteristicaDTO dto) {
        AvaliacaoCaracteristica atualizada = avaliacaoService.atualizar(dto);
        return ResponseEntity.ok(new AvaliacaoCaracteristicaDTO(atualizada));
    }

    @DeleteMapping("/aluno/{alunoId}/caracteristica/{caracteristicaId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long alunoId, @PathVariable Long caracteristicaId) {
        avaliacaoService.deletar(alunoId, caracteristicaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/aluno/{alunoId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ALUNO')")
    public ResponseEntity<List<AvaliacaoCaracteristicaDTO>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(avaliacaoService.listarPorAluno(alunoId));
    }

    @GetMapping("/caracteristica/{caracteristicaId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ALUNO')")
    public ResponseEntity<List<AvaliacaoCaracteristicaDTO>> listarPorCaracteristica(@PathVariable Long caracteristicaId) {
        return ResponseEntity.ok(avaliacaoService.listarPorCaracteristica(caracteristicaId));
    }
}
