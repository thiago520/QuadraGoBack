package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {
}
