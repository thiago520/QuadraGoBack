package com.quadrago.backend.models;

import com.quadrago.backend.enums.LessonStatus;
import com.quadrago.backend.models.base.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name = "lesson")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lesson extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Professor dono da aula (PK do teacher_profile = user_id) */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "user_id")
    private TeacherProfile teacherProfile;

    /** Aluno da aula (PK do student_profile = user_id) */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", referencedColumnName = "user_id")
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status;

    @Column(length = 1000)
    private String notes;
}
