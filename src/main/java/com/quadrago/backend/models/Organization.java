package com.quadrago.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {
    @Id
    @Column(name = "party_id")
    private Long partyId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "party_id")
    private Party party;

    @Column(nullable = false)
    private String corporateName;
    @Column(unique = true)
    private String cnpj;
    private String stateInsc;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}
