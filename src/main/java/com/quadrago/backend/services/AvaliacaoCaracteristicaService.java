package com.quadrago.backend.services;

import com.quadrago.backend.dtos.AvaliacaoCaracteristicaDTO;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.models.AvaliacaoCaracteristica;
import com.quadrago.backend.models.AvaliacaoCaracteristicaId;
import com.quadrago.backend.models.Caracteristica;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.AvaliacaoCaracteristicaRepository;
import com.quadrago.backend.repositories.CaracteristicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AvaliacaoCaracteristicaService {

    private final AvaliacaoCaracteristicaRepository avaliacaoRepository;
    private final CaracteristicaRepository caracteristicaRepository;
    private final AlunoRepository alunoRepository;

    public AvaliacaoCaracteristica salvar(AvaliacaoCaracteristicaDTO dto) {
        Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                .orElseThrow(() -> new NoSuchElementException("Aluno não encontrado"));

        Caracteristica caracteristica = caracteristicaRepository.findById(dto.getCaracteristicaId())
                .orElseThrow(() -> new NoSuchElementException("Característica não encontrada"));

        AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId(dto.getAlunoId(), dto.getCaracteristicaId());

        AvaliacaoCaracteristica avaliacao = AvaliacaoCaracteristica.builder()
                .id(id)
                .aluno(aluno)
                .caracteristica(caracteristica)
                .nota(dto.getNota())
                .build();

        return avaliacaoRepository.save(avaliacao);
    }

    public AvaliacaoCaracteristica atualizar(AvaliacaoCaracteristicaDTO dto) {
        AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId(dto.getAlunoId(), dto.getCaracteristicaId());

        AvaliacaoCaracteristica existente = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Avaliação não encontrada"));

        existente.setNota(dto.getNota());

        return avaliacaoRepository.save(existente);
    }

    public boolean deletar(Long alunoId, Long caracteristicaId) {
        AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId(alunoId, caracteristicaId);

        return avaliacaoRepository.findById(id).map(a -> {
            avaliacaoRepository.delete(a);
            return true;
        }).orElse(false);
    }

    public List<AvaliacaoCaracteristicaDTO> listarPorAluno(Long alunoId) {
        return avaliacaoRepository.findByAlunoId(alunoId).stream()
                .map(AvaliacaoCaracteristicaDTO::new)
                .toList();
    }

    public List<AvaliacaoCaracteristicaDTO> listarPorCaracteristica(Long caracteristicaId) {
        return avaliacaoRepository.findByCaracteristicaId(caracteristicaId).stream()
                .map(AvaliacaoCaracteristicaDTO::new)
                .toList();
    }
}

