package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Payment; // ajuste se necessário
import com.quadrago.backend.repositories.views.PaymentActivityView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
      select p.payer.name as payerName,
             p.createdAt as happenedAt
      from Payment p
      where p.teacher.user.id = :teacherUserId
      order by p.createdAt desc
      """)
    List<PaymentActivityView> findRecentPayments(@Param("teacherUserId") Long teacherUserId,
                                                 Pageable pageable);
    // Ajuste payer/name/relacionamento para o seu modelo real.
}