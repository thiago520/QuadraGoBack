package com.quadrago.backend.services;

import com.quadrago.backend.dtos.AlunoDTO;
import com.quadrago.backend.dtos.AlunoResponseDTO;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;

    public Aluno salvar(AlunoDTO dto) {
        Aluno aluno = new Aluno(
                null,
                dto.getNome(),
                dto.getCpf(),
                dto.getEmail(),
                dto.getTelefone(),
                resolveProfessores(dto)
        );
        return alunoRepository.save(aluno);
    }

    // ✅ ATUALIZADO: retorna DTO
    public List<AlunoResponseDTO> listar() {
        return alunoRepository.findAll().stream()
                .map(AlunoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<Aluno> buscarPorId(Long id) {
        return alunoRepository.findById(id);
    }

    public Optional<Aluno> atualizar(Long id, AlunoDTO dto) {
        return alunoRepository.findById(id).map(aluno -> {
            aluno.setNome(dto.getNome());
            aluno.setCpf(dto.getCpf());
            aluno.setEmail(dto.getEmail());
            aluno.setTelefone(dto.getTelefone());
            aluno.setProfessores(resolveProfessores(dto));
            return alunoRepository.save(aluno);
        });
    }

    public boolean deletar(Long id) {
        return alunoRepository.findById(id).map(aluno -> {
            alunoRepository.delete(aluno);
            return true;
        }).orElse(false);
    }

    private Set<Professor> resolveProfessores(AlunoDTO dto) {
        if (dto.getProfessoresIds() == null) return new HashSet<>();
        return new HashSet<>(professorRepository.findAllById(dto.getProfessoresIds()));
    }
}