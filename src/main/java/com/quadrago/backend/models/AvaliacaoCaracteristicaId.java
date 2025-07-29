package com.quadrago.backend.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoCaracteristicaId implements Serializable {

    private Long alunoId;
    private Long caracteristicaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvaliacaoCaracteristicaId)) return false;
        AvaliacaoCaracteristicaId that = (AvaliacaoCaracteristicaId) o;
        return Objects.equals(alunoId, that.alunoId) &&
                Objects.equals(caracteristicaId, that.caracteristicaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alunoId, caracteristicaId);
    }
}
