package com.quadrago.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name = "teacher_profile")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeacherProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(optional = false) @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private BigDecimal hourlyRate;

    @ElementCollection
    @CollectionTable(name = "teacher_specialties", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "specialty")
    private java.util.Set<String> specialties = new java.util.HashSet<>();

    @Column(nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @PreUpdate void touch() { this.updatedAt = OffsetDateTime.now(); }
}
