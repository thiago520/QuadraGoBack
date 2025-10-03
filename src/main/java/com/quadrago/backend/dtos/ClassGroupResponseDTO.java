package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.Level;
import com.quadrago.backend.models.ClassGroup;
import com.quadrago.backend.models.ClassSchedule;
import com.quadrago.backend.models.Student;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ClassGroupResponseDTO {

    private Long id;
    private String name;
    private Level level;
    private TeacherResumeDTO teacher;
    private Set<StudentResumeDTO> students;   // plural
    private Set<ClassSchedule> schedules;

    /** Computed level based on students' trait evaluations average */
    private Level computedLevel;

    public ClassGroupResponseDTO(ClassGroup classGroup) {
        this.id = classGroup.getId();
        this.name = classGroup.getName();
        this.level = classGroup.getLevel();
        this.teacher = TeacherResumeDTO.of(classGroup.getTeacher());

        this.students = classGroup.getStudents() == null ? Set.of()
                : classGroup.getStudents().stream()
                .map(StudentResumeDTO::of)   // << usar factory, não construtor
                .collect(Collectors.toSet());

        this.schedules = classGroup.getSchedules();
        this.computedLevel = computeLevel(classGroup);
    }

    private Level computeLevel(ClassGroup classGroup) {
        Set<Student> students = classGroup.getStudents();
        if (students == null || students.isEmpty()) {
            return Level.BEGINNER;
        }

        List<Integer> allScores = students.stream()
                .flatMap(st -> st.getTraitEvaluations().stream())
                .map(te -> te.getScore() != null ? te.getScore() : 0)
                .toList();

        if (allScores.isEmpty()) return Level.BEGINNER;

        double average = allScores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        if (average <= 3) return Level.BEGINNER;
        if (average <= 7) return Level.INTERMEDIATE;
        return Level.ADVANCED;
    }
}
