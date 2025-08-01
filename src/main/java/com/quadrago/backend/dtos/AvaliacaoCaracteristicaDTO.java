package com.quadrago.backend.dtos;

import com.quadrago.backend.models.AvaliacaoCaracteristica;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoCaracteristicaDTO {

    @NotNull(message = "ID do aluno é obrigatório")
    private Long alunoId;

    @NotNull(message = "ID da característica é obrigatório")
    private Long caracteristicaId;

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 0, message = "Nota mínima é 0")
    @Max(value = 10, message = "Nota máxima é 10")
    private Integer nota;


    public AvaliacaoCaracteristicaDTO(AvaliacaoCaracteristica avaliacao) {
        this.alunoId = avaliacao.getAluno().getId();
        this.caracteristicaId = avaliacao.getCaracteristica().getId();
        this.nota = avaliacao.getNota(); // ou getNota(), dependendo do nome do campo na entidade
    }

}
