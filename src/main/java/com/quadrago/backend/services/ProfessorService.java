package com.quadrago.backend.services;

import com.quadrago.backend.dtos.ProfessorDTO;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository repository;

    public Professor salvar(ProfessorDTO dto) {
        Professor p = new Professor(null, dto.getNome(), dto.getTelefone(), dto.getCpf());
        return repository.save(p);
    }

    public List<Professor> listar() {
        return repository.findAll();
    }

    public Optional<Professor> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public boolean deletar(Long id) {
        return repository.findById(id).map(professor -> {
            repository.delete(professor);
            return true;
        }).orElse(false);
    }

    public Optional<Professor> atualizar(Long id, ProfessorDTO dto) {
        return repository.findById(id).map(prof -> {
            prof.setNome(dto.getNome());
            prof.setTelefone(dto.getTelefone());
            prof.setCpf(dto.getCpf());
            return repository.save(prof);
        });
    }
}
