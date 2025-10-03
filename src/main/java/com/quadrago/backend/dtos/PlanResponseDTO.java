package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.SubscriptionPeriod;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanResponseDTO {
    private Long id;
    private Long teacherId;
    private String icon;
    private String title;
    private BigDecimal price;
    private SubscriptionPeriod period;
    private boolean active;
    private List<String> features;
}
