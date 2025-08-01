package com.quadrago.backend.services;

import com.quadrago.backend.dtos.CaracteristicaDTO;
import com.quadrago.backend.models.Caracteristica;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.CaracteristicaRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CaracteristicaService {

    private final CaracteristicaRepository caracteristicaRepository;
    private final ProfessorRepository professorRepository;

    public CaracteristicaDTO salvar(CaracteristicaDTO dto) {
        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new NoSuchElementException("Professor não encontrado"));

        Caracteristica caracteristica = new Caracteristica();
        caracteristica.setNome(dto.getNome());
        caracteristica.setProfessor(professor);

        return new CaracteristicaDTO(caracteristicaRepository.save(caracteristica));
    }

    public CaracteristicaDTO atualizar(Long id, CaracteristicaDTO dto) {
        Caracteristica existente = caracteristicaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Característica não encontrada"));

        existente.setNome(dto.getNome());

        // Se o professor puder ser alterado:
        if (dto.getProfessorId() != null) {
            Professor professor = professorRepository.findById(dto.getProfessorId())
                    .orElseThrow(() -> new NoSuchElementException("Professor não encontrado"));
            existente.setProfessor(professor);
        }

        return new CaracteristicaDTO(caracteristicaRepository.save(existente));
    }

    public List<CaracteristicaDTO> listarPorProfessor(Long professorId) {
        return caracteristicaRepository.findByProfessorId(professorId).stream()
                .map(CaracteristicaDTO::new)
                .toList();
    }

    public boolean deletar(Long id) {
        return caracteristicaRepository.findById(id).map(c -> {
            caracteristicaRepository.delete(c);
            return true;
        }).orElse(false);
    }
}
