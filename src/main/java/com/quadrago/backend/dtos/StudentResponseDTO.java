package com.quadrago.backend.dtos;

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
public class StudentResponseDTO {

    private Long id;
    private String name;
    private String nationalId; // was 'cpf'
    private String email;
    private String phone;

    private Set<TeacherResumeDTO> teachers;
    private List<TraitEvaluationDTO> evaluations;

    public StudentResponseDTO(Student student) {
        this.id = student.getId();
        this.name = student.getName();
        this.nationalId = student.getNationalId();
        this.email = student.getEmail();
        this.phone = student.getPhone();

        this.teachers = student.getTeachers() == null ? Set.of()
                : student.getTeachers().stream()
                .map(TeacherResumeDTO::new)
                .collect(Collectors.toSet());

        this.evaluations = student.getTraitEvaluations() == null ? List.of()
                : student.getTraitEvaluations().stream()
                .map(TraitEvaluationDTO::new) // assume existir construtor DTO(Student/TraitEvaluation)
                .collect(Collectors.toList());
    }
}
