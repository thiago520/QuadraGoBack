package com.quadrago.backend.repositories;

import com.quadrago.backend.models.AvaliacaoCaracteristica;
import com.quadrago.backend.models.AvaliacaoCaracteristicaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoCaracteristicaRepository extends JpaRepository<AvaliacaoCaracteristica, AvaliacaoCaracteristicaId> {

    List<AvaliacaoCaracteristica> findByCaracteristicaId(Long caracteristicaId);

    List<AvaliacaoCaracteristica> findByAlunoId(Long alunoId);

    List<AvaliacaoCaracteristica> findByCaracteristicaProfessorId(Long professorId); // opcional se quiser buscar por professor
}
