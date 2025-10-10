package com.quadrago.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name = "person")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Person {
    @Id
    @Column(name = "party_id")
    private Long partyId;

    @OneToOne(optional = false) @MapsId
    @JoinColumn(name = "party_id")
    private Party party;

    @Column(nullable = false) private String name;
    @Column(unique = true) private String cpf;
    private String phone;
    private java.time.LocalDate birthDate;

    @Column(nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @PreUpdate void touch() { this.updatedAt = OffsetDateTime.now(); }
}
