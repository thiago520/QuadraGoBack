package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    @Query("SELECT a FROM Aluno a LEFT JOIN FETCH a.professores")
    List<Aluno> findAllWithProfessores();

}
