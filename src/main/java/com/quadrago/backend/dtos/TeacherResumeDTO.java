package com.quadrago.backend.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quadrago.backend.models.Teacher;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherResumeDTO {

    private Long id;
    private String name;
    private String email;

    public static TeacherResumeDTO of(Teacher teacher) {
        if (teacher == null) return null;
        return TeacherResumeDTO.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .build();
    }
}
