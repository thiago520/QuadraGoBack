package com.quadrago.backend.models;

import com.quadrago.backend.enums.SubscriptionStatus;
import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name = "subscription")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Professor dono da assinatura */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "user_id")
    private TeacherProfile teacherProfile;

    /** Aluno assinante */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private StudentProfile studentProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    /** Dados simples de “plano” (pode  evoluir para entidade própria) */
    @Column(nullable = false)
    private String planName;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private OffsetDateTime nextRenewAt;
}
