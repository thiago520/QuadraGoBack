package com.quadrago.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@Entity
@Table(name = "professores")
@Getter
@Setter
@ToString(exclude = "alunos") // evita loop infinito no log
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    private String telefone;

    private String cpf;

    @ManyToMany(mappedBy = "professores")
    @JsonBackReference
    private Set<Aluno> alunos = new HashSet<>();

    @OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Caracteristica> caracteristicas = new HashSet<>();


}
