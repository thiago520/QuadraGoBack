package com.quadrago.backend.models;

import com.quadrago.backend.enums.Level;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "classgroup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"teacher", "students", "schedules"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Group name (e.g., "Beginner A") */
    private String name;

    @Enumerated(EnumType.STRING)
    private Level level;

    /** Many groups can belong to one teacher */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Students enrolled in this class group.
     * FIX: inverseJoinColumns must reference the STUDENT id column (was classgroup_id by mistake).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "classgroup_student",
            joinColumns = @JoinColumn(name = "classgroup_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    /**
     * Weekly schedules (embeddable value objects).
     * Suggest LAZY to avoid n+1 and oversized payloads during listing.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "classgroup_schedules",
            joinColumns = @JoinColumn(name = "classgroup_id")
    )
    @Builder.Default
    private Set<ClassSchedule> schedules = new HashSet<>();
}
