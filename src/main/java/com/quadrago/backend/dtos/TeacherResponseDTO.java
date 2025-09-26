package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Teacher;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeacherResponseDTO {

    private Long id;
    private String name;

    public TeacherResponseDTO(Teacher teacher) {
        this.id = teacher.getId();
        this.name = teacher.getName();
    }
}
