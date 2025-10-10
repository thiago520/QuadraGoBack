package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
