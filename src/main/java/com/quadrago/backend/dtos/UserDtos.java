package com.quadrago.backend.dtos;

import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class UserDtos {

    /* ======= CREATE ======= */
    public record CreatePersonUserRequest(
            @NotBlank String name,
            String cpf,
            String phone,
            LocalDate birthDate,
            @Email @NotBlank String email,
            @NotBlank String password,
            Set<RoleName> roles
    ) {
    }

    /* ======= READ/RESPONSE ======= */

    /**
     * Resposta simples (usada em listas)
     */
    public record UserResponse(
            Long id,
            String email,
            String name
    ) {
    }

    /**
     * Resposta detalhada (status, roles e presença de perfis)
     */
    public record UserDetailsResponse(
            Long id,
            String email,
            String name,
            UserStatus status,
            Set<RoleName> roles,
            boolean hasTeacherProfile,
            boolean hasStudentProfile
    ) {
    }

    /* ======= UPDATE - USER ======= */
    public record UpdateEmailRequest(
            @Email @NotBlank String email
    ) {
    }

    public record UpdatePasswordRequest(
            @NotBlank String password
    ) {
    }

    public record UpdateStatusRequest(
            @NotNull UserStatus status
    ) {
    }

    public record UpdatePersonDataRequest(
            String name,
            String cpf,
            String phone,
            LocalDate birthDate
    ) {
    }

    /* ======= ROLES (RBAC) ======= */
    public record RolesRequest(
            @NotNull Set<RoleName> roles
    ) {
    }

    /* ======= PROFILES (Requests/Responses) ======= */
    public record CreateTeacherProfileRequest(
            String bio,
            BigDecimal hourlyRate,
            Set<String> specialties
    ) {
    }

    public record UpdateTeacherProfileRequest(
            String bio,
            BigDecimal hourlyRate,
            Set<String> specialties
    ) {
    }

    public record TeacherProfileResponse(
            Long userId,
            String bio,
            BigDecimal hourlyRate,
            Set<String> specialties
    ) {
    }

    public record CreateStudentProfileRequest(
            String emergencyContact,
            String notes
    ) {
    }

    public record UpdateStudentProfileRequest(
            String emergencyContact,
            String notes
    ) {
    }

    public record StudentProfileResponse(
            Long userId,
            String emergencyContact,
            String notes
    ) {
    }
}
