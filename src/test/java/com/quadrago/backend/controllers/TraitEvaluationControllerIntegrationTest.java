package com.quadrago.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrago.backend.BackendApplication;
import com.quadrago.backend.config.TestSecurityConfig;
import com.quadrago.backend.dtos.TraitEvaluationDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import com.quadrago.backend.repositories.TraitEvaluationRepository;
import com.quadrago.backend.repositories.TraitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class) // PasswordEncoder + SecurityFilterChain leve + @EnableMethodSecurity
class TraitEvaluationControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired StudentRepository studentRepository;
    @Autowired TeacherRepository teacherRepository;
    @Autowired TraitRepository traitRepository;
    @Autowired TraitEvaluationRepository evaluationRepository;

    private Long studentId;
    private Long traitId;
    private Long teacherId;

    @BeforeEach
    @Transactional
    void setup() {
        // Limpeza em ordem segura (evita violações de FK)
        evaluationRepository.deleteAllInBatch();
        traitRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        teacherRepository.deleteAllInBatch();

        // Cenário base: Teacher -> Trait; Student isolado
        Teacher teacher = teacherRepository.save(
                Teacher.builder()
                        .name("Teacher Test")
                        .email("owner@exemplo.com")
                        .nationalId("11122233344")
                        .phone("11999990000")
                        .build()
        );
        teacherId = teacher.getId();

        Trait trait = traitRepository.save(
                Trait.builder()
                        .name("Focus")
                        .teacher(teacher)
                        .build()
        );
        traitId = trait.getId();

        Student student = studentRepository.save(
                Student.builder()
                        .name("Alice")
                        .nationalId("99988877766")
                        .email("alice@test.com")
                        .phone("11988887777")
                        .build()
        );
        studentId = student.getId();
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldCreateEvaluation() throws Exception {
        TraitEvaluationDTO dto = new TraitEvaluationDTO();
        dto.setStudentId(studentId);
        dto.setTraitId(traitId);
        dto.setScore(8);

        mvc.perform(post("/trait-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldForbidCreateForStudentRole() throws Exception {
        TraitEvaluationDTO dto = new TraitEvaluationDTO();
        dto.setStudentId(studentId);
        dto.setTraitId(traitId);
        dto.setScore(7);

        mvc.perform(post("/trait-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
