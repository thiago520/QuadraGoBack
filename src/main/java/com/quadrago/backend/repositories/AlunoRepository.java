package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
}
