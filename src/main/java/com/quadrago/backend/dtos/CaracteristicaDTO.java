package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Caracteristica;
import lombok.Data;

@Data
public class CaracteristicaDTO {

    private Long id;
    private String nome;
    private Long professorId;

    public CaracteristicaDTO(Caracteristica caracteristica) {
        this.id = caracteristica.getId();
        this.nome = caracteristica.getNome();
        this.professorId = caracteristica.getProfessor().getId();
    }

    public CaracteristicaDTO() {

    }

}
