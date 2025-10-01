package com.quadrago.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrago.backend.BackendApplication;
import com.quadrago.backend.config.TestSecurityConfig;
import com.quadrago.backend.dtos.TraitDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TraitEvaluationRepository;
import com.quadrago.backend.repositories.TraitRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TraitControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private TeacherRepository teacherRepository;
    @Autowired private TraitRepository traitRepository;

    // adicionados para limpeza segura
    @Autowired private StudentRepository studentRepository;
    @Autowired private TraitEvaluationRepository evaluationRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private JdbcTemplate jdbcTemplate; // <<< ADICIONE

    private Long teacherId;

    @BeforeEach
    @Transactional
    void setup() {
        // 1) Apague dependentes primeiro (ordem segura)
        evaluationRepository.deleteAllInBatch(); // se houver avaliações ligadas a traits/students
        traitRepository.deleteAllInBatch();      // traits dependem de teacher

        // 2) Limpa a join sem precisar de transação JPA
        jdbcTemplate.update("DELETE FROM student_teacher");

        // 3) Agora é seguro apagar bases
        teacherRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();

        // 4) Recria cenário base
        Teacher teacher = Teacher.builder()
                .name("Teacher Test")
                .email("owner@exemplo.com")
                .nationalId("12345678900")
                .phone("11999999999")
                .build();

        teacherId = teacherRepository.save(teacher).getId();
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldCreateTrait() throws Exception {
        TraitDTO dto = new TraitDTO();
        dto.setName("Endurance");
        dto.setTeacherId(teacherId);

        mockMvc.perform(post("/traits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Endurance")))
                .andExpect(jsonPath("$.teacherId", is(teacherId.intValue())));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldListTraitsByTeacher() throws Exception {
        traitRepository.save(Trait.builder().name("Speed").teacher(getTeacher()).traitEvaluations(new HashSet<>()).build());
        traitRepository.save(Trait.builder().name("Endurance").teacher(getTeacher()).traitEvaluations(new HashSet<>()).build());

        mockMvc.perform(get("/traits/teacher/" + teacherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldUpdateTrait() throws Exception {
        Trait trait = traitRepository.save(
                Trait.builder().name("Focus").teacher(getTeacher()).traitEvaluations(new HashSet<>()).build()
        );

        TraitDTO dto = new TraitDTO();
        dto.setName("Discipline");
        dto.setTeacherId(teacherId);

        mockMvc.perform(put("/traits/{id}", trait.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Discipline")))
                .andExpect(jsonPath("$.teacherId", is(teacherId.intValue())));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldDeleteTrait() throws Exception {
        Trait trait = traitRepository.save(
                Trait.builder().name("Concentration").teacher(getTeacher()).traitEvaluations(new HashSet<>()).build()
        );

        mockMvc.perform(delete("/traits/{id}", trait.getId()))
                .andExpect(status().isNoContent());

        assertTrue(traitRepository.findById(trait.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldForbidCreateForStudentRole() throws Exception {
        TraitDTO dto = new TraitDTO();
        dto.setName("Strength");
        dto.setTeacherId(teacherId);

        mockMvc.perform(post("/traits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    private Teacher getTeacher() {
        return teacherRepository.findById(teacherId).orElseThrow();
    }
}
