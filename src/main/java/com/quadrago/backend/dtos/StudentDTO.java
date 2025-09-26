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

    /** Brazilian CPF or other national id: exactly 11 numeric digits */
    @NotBlank(message = "nationalId is required")
    @Pattern(regexp = "\\d{11}", message = "nationalId must have 11 digits")
    private String nationalId;

    @Email(message = "email must be valid")
    private String email;

    /** E.164-like simple numeric check: 10 or 11 digits */
    @Pattern(regexp = "\\d{10,11}", message = "phone must have 10 or 11 digits")
    private String phone;

    /** Optional: teachers to associate with this student */
    private Set<Long> teacherIds;
}
