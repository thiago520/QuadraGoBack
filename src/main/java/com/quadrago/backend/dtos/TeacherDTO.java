package com.quadrago.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDTO {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "\\d{10,11}", message = "phone must have 10 or 11 numeric digits")
    private String phone;

    @NotBlank(message = "nationalId is required")
    @Pattern(regexp = "\\d{11}", message = "nationalId must have 11 numeric digits")
    private String nationalId; // mapped to column 'cpf' in the entity
}
