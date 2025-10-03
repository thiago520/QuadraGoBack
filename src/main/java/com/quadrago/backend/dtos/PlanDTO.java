package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.SubscriptionPeriod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanDTO {
    @NotBlank
    private String icon;

    @NotBlank
    private String title;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;

    @NotNull
    private SubscriptionPeriod period;

    /** mínimo 1 característica */
    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> features;
}
