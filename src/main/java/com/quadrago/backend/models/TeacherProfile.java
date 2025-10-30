package com.quadrago.backend.models;

import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "teacher_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherProfile extends Timestamped {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private BigDecimal hourlyRate;

    @ElementCollection
    @CollectionTable(name = "teacher_specialties", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "specialty")
    private java.util.Set<String> specialties = new java.util.HashSet<>();
}
