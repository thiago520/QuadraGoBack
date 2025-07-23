package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@Entity
@Table(name = "alunos")
@Getter
@Setter
@ToString(exclude = "professores") // evita loop infinito no log
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(unique = true)
    private String email;

    private String telefone;

    @Column(nullable = false)
    private Integer pontuacao = 0;

    @ManyToMany
    @JoinTable(
            name = "aluno_professor",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "professor_id")
    )
    @JsonManagedReference
    private Set<Professor> professores = new HashSet<>();

}
