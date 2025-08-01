package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.CaracteristicaDTO;
import com.quadrago.backend.services.CaracteristicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/caracteristicas")
@RequiredArgsConstructor
public class CaracteristicaController {

    private final CaracteristicaService caracteristicaService;

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<CaracteristicaDTO> criar(@RequestBody @Valid CaracteristicaDTO dto) {
        CaracteristicaDTO criada = caracteristicaService.salvar(dto);
        return ResponseEntity.status(201).body(criada);
    }

    @GetMapping("/professor/{professorId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public ResponseEntity<List<CaracteristicaDTO>> listarPorProfessor(@PathVariable Long professorId) {
        return ResponseEntity.ok(caracteristicaService.listarPorProfessor(professorId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<CaracteristicaDTO> atualizar(@PathVariable Long id, @RequestBody @Valid CaracteristicaDTO dto) {
        return ResponseEntity.ok(caracteristicaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        caracteristicaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
