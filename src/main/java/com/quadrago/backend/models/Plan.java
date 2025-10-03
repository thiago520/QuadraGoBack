package com.quadrago.backend.models;

import com.quadrago.backend.enums.SubscriptionPeriod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "plans",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_plans_teacher_title",
                columnNames = {"teacher_id", "title"}
        ),
        indexes = {
                @Index(name = "idx_plans_teacher", columnList = "teacher_id"),
                @Index(name = "idx_plans_active", columnList = "active")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dono do plano */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_teacher"))
    private Teacher teacher;

    /** Nome do ícone do Material (ex.: "fitness_center") */
    @NotBlank
    @Column(name = "icon", nullable = false, length = 80)
    private String icon;

    @NotBlank
    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 15)
    private SubscriptionPeriod period;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Características (mínimo de 1 no DTO) */
    @OneToMany(
            mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<PlanFeature> features = new ArrayList<>();

    /* helpers */
    public void addFeature(PlanFeature f) {
        f.setPlan(this);
        features.add(f);
    }

    public void clearAndAddFeatures(List<PlanFeature> items) {
        features.clear();
        if (items != null) {
            items.forEach(this::addFeature);
        }
    }
}
