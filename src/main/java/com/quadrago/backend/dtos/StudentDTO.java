package com.quadrago.backend.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    @NotBlank(message = "name is required")
    private String name;

    /** Brazilian CPF (11 dígitos) */
    @NotBlank(message = "nationalId is required")
    @Pattern(regexp = "\\d{11}", message = "nationalId must have 11 digits")
    private String nationalId;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 120, message = "password must be between 8 and 120 characters")
    private String password;

    /** E.164-like simples: 10 ou 11 dígitos (opcional) */
    @Pattern(regexp = "\\d{10,11}", message = "phone must have 10 or 11 digits")
    private String phone;

    /** Opcional: professores a associar */
    private Set<Long> teacherIds;
}
