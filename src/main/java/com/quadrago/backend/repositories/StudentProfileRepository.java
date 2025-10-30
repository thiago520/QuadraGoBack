package com.quadrago.backend.repositories;

import com.quadrago.backend.models.StudentProfile; // ajuste se o nome do modelo diferir
import com.quadrago.backend.repositories.views.EnrollmentActivityView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    // Conta alunos do professor logado
    @Query("""
      select count(sp) from StudentProfile sp
      where sp.teacher.user.id = :teacherUserId
      """)
    long countByTeacherUserId(@Param("teacherUserId") Long teacherUserId);
    // Se sua relação for sp.teacherId (Long):
    // where sp.teacherId = :teacherUserId

    // Matrículas recentes (nome + data)
    @Query("""
      select sp.person.name as studentName,
             sp.createdAt as happenedAt
      from StudentProfile sp
      where sp.teacher.user.id = :teacherUserId
      order by sp.createdAt desc
      """)
    List<EnrollmentActivityView> findRecentEnrollments(@Param("teacherUserId") Long teacherUserId,
                                                       Pageable pageable);
    // Alternativa de nome: sp.user.name, sp.student.name, etc.
}
