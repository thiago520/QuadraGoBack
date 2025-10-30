package com.quadrago.backend.models;

import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends Timestamped {
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
}
