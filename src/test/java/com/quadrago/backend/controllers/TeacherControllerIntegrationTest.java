package com.quadrago.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrago.backend.BackendApplication;
import com.quadrago.backend.config.TestSecurityConfig;
import com.quadrago.backend.dtos.TeacherDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TraitEvaluationRepository;
import com.quadrago.backend.repositories.TraitRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TeacherControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private TeacherRepository teacherRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private TraitRepository traitRepository;
    @Autowired private TraitEvaluationRepository evaluationRepository;

    // Usar JdbcTemplate para limpar join table sem transação JPA
    @Autowired private JdbcTemplate jdbcTemplate;

    private Long teacherId;

    @BeforeEach
    void setup() {
        // 1) Dependentes primeiro
        evaluationRepository.deleteAllInBatch(); // avaliações dependem de traits/students
        traitRepository.deleteAllInBatch();      // traits dependem de teacher

        // 2) Limpa a join table sem necessidade de transação JPA
        jdbcTemplate.update("DELETE FROM student_teacher");

        // 3) Bases
        teacherRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();

        // 4) Cenário base: teacher existente
        Teacher t = Teacher.builder()
                .name("Carlos Silva")
                .email("carlos.silva@exemplo.com")
                .phone("11987654321")
                .nationalId("12345678901")
                .build();
        t = teacherRepository.save(t);
        teacherId = t.getId();
    }

    // ----- LIST / GET protegidos -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListTeachers() throws Exception {
        mockMvc.perform(get("/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Carlos Silva")))
                .andExpect(jsonPath("$[0].email", is("carlos.silva@exemplo.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetTeacherById() throws Exception {
        mockMvc.perform(get("/teachers/{id}", teacherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Carlos Silva")))
                .andExpect(jsonPath("$.email", is("carlos.silva@exemplo.com")))
                .andExpect(jsonPath("$.nationalId", is("12345678901")));
    }

    // ----- CREATE público (sem autenticação) -----

    @Test
    void shouldCreateTeacher_Public_NoAuth_201() throws Exception {
        TeacherDTO dto = TeacherDTO.builder()
                .name("Joao da Silva")
                .email("joao.silva@exemplo.com")
                .phone("11988887777")
                .nationalId("98765432100")
                .build();

        mockMvc.perform(post("/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/teachers/")))
                .andExpect(jsonPath("$.name", is("Joao da Silva")))
                .andExpect(jsonPath("$.email", is("joao.silva@exemplo.com")))
                .andExpect(jsonPath("$.phone", is("11988887777")))
                .andExpect(jsonPath("$.nationalId", is("98765432100")));
    }

    @Test
    void shouldReturn400WhenCreateWithInvalidBody_Public() throws Exception {
        // DTO vazio viola @NotBlank/@Email/@Pattern
        TeacherDTO invalid = new TeacherDTO();

        mockMvc.perform(post("/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    // ----- UPDATE / DELETE protegidos -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateTeacher() throws Exception {
        TeacherDTO dto = TeacherDTO.builder()
                .name("Updated Name")
                .email("carlos.updated@exemplo.com")
                .phone("11977776666")
                .nationalId("99988877766")
                .build();

        mockMvc.perform(put("/teachers/{id}", teacherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("carlos.updated@exemplo.com")))
                .andExpect(jsonPath("$.phone", is("11977776666")))
                .andExpect(jsonPath("$.nationalId", is("99988877766")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTeacher() throws Exception {
        mockMvc.perform(delete("/teachers/{id}", teacherId))
                .andExpect(status().isNoContent());

        Optional<Teacher> deleted = teacherRepository.findById(teacherId);
        assertTrue(deleted.isEmpty(), "Teacher should have been deleted");
    }

    // ----- CENÁRIOS DE ERRO / AUTORIZAÇÃO -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenTeacherNotFound() throws Exception {
        mockMvc.perform(get("/teachers/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserHasNoDeletePermission() throws Exception {
        mockMvc.perform(delete("/teachers/{id}", teacherId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserHasNoListPermission() throws Exception {
        mockMvc.perform(get("/teachers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenNotAuthenticatedOnProtectedEndpoint() throws Exception {
        // delete continua protegido, deve exigir autenticação
        mockMvc.perform(delete("/teachers/{id}", teacherId))
                .andExpect(status().isUnauthorized());
    }
}
