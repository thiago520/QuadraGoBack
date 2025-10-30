package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Subscription;
import com.quadrago.backend.repositories.views.EnrollmentActivityView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("""
      select count(s) from Subscription s
      where s.teacherProfile.user.id = :teacherUserId
        and s.status = 'ACTIVE'
    """)
    long countActiveByTeacherUserId(@Param("teacherUserId") Long teacherUserId);

    @Query("""
      select count(distinct s.studentProfile.user.id) from Subscription s
      where s.teacherProfile.user.id = :teacherUserId
        and s.status = 'ACTIVE'
    """)
    long countDistinctActiveStudents(@Param("teacherUserId") Long teacherUserId);

    @Query("""
      select s.studentProfile.user.email as studentEmail,
             s.createdAt as happenedAt
      from Subscription s
      where s.teacherProfile.user.id = :teacherUserId
      order by s.createdAt desc
    """)
    List<EnrollmentActivityView> findRecentEnrollments(@Param("teacherUserId") Long teacherUserId,
                                                       Pageable pageable);
}
