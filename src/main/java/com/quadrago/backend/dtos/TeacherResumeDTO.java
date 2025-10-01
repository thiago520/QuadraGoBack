package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Teacher;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeacherResumeDTO {

    private Long id;
    private String name;
    private String email;

    public TeacherResumeDTO(Teacher teacher) {
        this.id = teacher.getId();
        this.name = teacher.getName();
        this.email = teacher.getEmail();
    }
}
