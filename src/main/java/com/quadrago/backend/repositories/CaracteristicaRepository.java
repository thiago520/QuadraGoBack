package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Long> {

    List<Caracteristica> findByProfessorId(Long professorId);

}
