package com.quadrago.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoCaracteristica {

    @EmbeddedId
    private AvaliacaoCaracteristicaId id = new AvaliacaoCaracteristicaId();

    @ManyToOne
    @MapsId("alunoId")
    private Aluno aluno;

    @ManyToOne
    @MapsId("caracteristicaId")
    private Caracteristica caracteristica;

    @Min(0)
    @Max(10)
    private Integer nota;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvaliacaoCaracteristica)) return false;
        AvaliacaoCaracteristica that = (AvaliacaoCaracteristica) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
