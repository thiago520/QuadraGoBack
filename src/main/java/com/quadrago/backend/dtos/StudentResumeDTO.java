package com.quadrago.backend.dtos;

import com.quadrago.backend.models.Student;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentResumeDTO {
    private Long id;
    private String name;

    public StudentResumeDTO(Student student) {
        this.id = student.getId();
        this.name = student.getName();
    }
}
