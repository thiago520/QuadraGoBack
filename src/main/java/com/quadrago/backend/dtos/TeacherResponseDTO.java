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
public class TeacherResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;       // opcional: útil ao front
    private String nationalId;  // opcional: útil ao front

    /** Mapeador seguro (password nunca é copiado) */
    public static TeacherResponseDTO of(Teacher teacher) {
        if (teacher == null) return null;
        return TeacherResponseDTO.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .nationalId(teacher.getNationalId())
                .build();
    }
}
