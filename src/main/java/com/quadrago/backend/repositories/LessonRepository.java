package com.quadrago.backend.repositories;

import com.quadrago.backend.models.Lesson;
import com.quadrago.backend.repositories.views.LessonActivityView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
      select count(l) from Lesson l
      where l.teacherProfile.user.id = :teacherUserId
        and l.startAt > :now
        and l.status in ('SCHEDULED','RESCHEDULED')
    """)
    long countFutureLessonsByTeacher(@Param("teacherUserId") Long teacherUserId,
                                     @Param("now") OffsetDateTime now);

    @Query("""
      select l.studentProfile.user.email as studentEmail,
             case when l.status = 'CANCELED' then 'Aula cancelada'
                  when l.status = 'RESCHEDULED' then 'Aula reagendada'
                  else 'Aula agendada' end as eventLabel,
             coalesce(l.updatedAt, l.createdAt) as happenedAt
      from Lesson l
      where l.teacherProfile.user.id = :teacherUserId
      order by happenedAt desc
    """)
    List<LessonActivityView> findRecentChanges(@Param("teacherUserId") Long teacherUserId,
                                               Pageable pageable);
}
