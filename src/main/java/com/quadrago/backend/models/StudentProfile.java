package com.quadrago.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name = "student_profile")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(optional = false) @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String emergencyContact;
    private String notes;

    @Column(nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @PreUpdate void touch() { this.updatedAt = OffsetDateTime.now(); }
}
