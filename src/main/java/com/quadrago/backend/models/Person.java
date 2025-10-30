package com.quadrago.backend.models;

import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "person")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Person extends Timestamped {
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

}
