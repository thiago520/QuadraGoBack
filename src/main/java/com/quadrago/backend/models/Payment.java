package com.quadrago.backend.models;

import com.quadrago.backend.enums.PaymentMethod;
import com.quadrago.backend.enums.PaymentStatus;
import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name = "payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Professor recebedor */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "user_id")
    private TeacherProfile teacherProfile;

    /** Aluno pagador */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private StudentProfile studentProfile;

    /** (Opcional) vínculo com a assinatura */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 3) // "BRL"
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private OffsetDateTime paidAt;

    @Column(length = 1000)
    private String description;
}
