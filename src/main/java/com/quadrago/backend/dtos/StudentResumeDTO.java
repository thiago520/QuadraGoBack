package com.quadrago.backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quadrago.backend.models.Student;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResumeDTO {
    private Long id;
    private String name;

    public static StudentResumeDTO of(Student student) {
        if (student == null) return null;
        return StudentResumeDTO.builder()
                .id(student.getId())
                .name(student.getName())
                .build();
    }
}
