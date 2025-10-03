package com.quadrago.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "plan_features", indexes = {
        @Index(name = "idx_plan_features_plan", columnList = "plan_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Plano ao qual pertence */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feature_plan"))
    private Plan plan;

    /** Texto da característica */
    @NotBlank
    @Column(name = "text", nullable = false, length = 255)
    private String text;

    /** Ordem para exibição */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
