package com.quadrago.backend.models;

import com.quadrago.backend.enums.PartyType;
import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "party")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType partyType;

}
