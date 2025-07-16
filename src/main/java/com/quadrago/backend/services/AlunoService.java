package com.quadrago.backend.services;

import com.quadrago.backend.dtos.AlunoDTO;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.repositories.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public Aluno salvar(AlunoDTO dto) {
        Aluno aluno = new Aluno(null, dto.getNome(), dto.getCpf(), dto.getEmail(), dto.getTelefone());
        return alunoRepository.save(aluno);
    }

    public List<Aluno> listar() {
        return alunoRepository.findAll();
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
            return alunoRepository.save(aluno);
        });
    }

    public boolean deletar(Long id) {
        return alunoRepository.findById(id).map(aluno -> {
            alunoRepository.delete(aluno);
            return true;
        }).orElse(false);
    }

}
