package com.quadrago.backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quadrago.backend.models.Student;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResponseDTO {

    private Long id;
    private String name;
    private String nationalId; // was 'cpf'
    private String email;
    private String phone;

    private Set<TeacherResumeDTO> teachers;
    private List<TraitEvaluationDTO> evaluations;

    /** Factory seguro (nunca inclui password) */
    public static StudentResponseDTO of(Student student) {
        if (student == null) return null;
        return StudentResponseDTO.builder()
                .id(student.getId())
                .name(student.getName())
                .nationalId(student.getNationalId())
                .email(student.getEmail())
                .phone(student.getPhone())
                .teachers(student.getTeachers() == null ? Set.of()
                        : student.getTeachers().stream()
                        .map(TeacherResumeDTO::of)      // <<< usa o factory, não o construtor
                        .collect(Collectors.toSet()))
                .evaluations(student.getTraitEvaluations() == null ? List.of()
                        : student.getTraitEvaluations().stream()
                        .map(TraitEvaluationDTO::new)   // supondo existir este construtor
                        .collect(Collectors.toList()))
                .build();
    }
}
