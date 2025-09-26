package com.quadrago.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrago.backend.BackendApplication;
import com.quadrago.backend.config.TestSecurityConfig;
import com.quadrago.backend.dtos.StudentDTO;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class) // <<— PasswordEncoder + SecurityFilterChain + @EnableMethodSecurity
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateStudent() throws Exception {
        Teacher teacher = teacherRepository.save(
                Teacher.builder()
                        .name("Prof. Test")
                        .phone("11999999999")
                        .nationalId("12345678901")
                        .build()
        );

        StudentDTO dto = new StudentDTO(
                "Joao",
                "12345678901",
                "joao@email.com",
                "11999999999",
                Set.of(teacher.getId())
        );

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Joao")))
                .andExpect(jsonPath("$.nationalId", is("12345678901")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateStudent() throws Exception {
        Teacher teacher = teacherRepository.save(
                Teacher.builder()
                        .name("Prof. Test")
                        .phone("11999999999")
                        .nationalId("12345678901")
                        .build()
        );

        Student student = studentRepository.save(
                Student.builder()
                        .name("Carlos")
                        .email("carlos@email.com")
                        .nationalId("88888888888")
                        .phone("7777777777")
                        .teachers(Set.of())
                        .build()
        );

        StudentDTO dto = new StudentDTO(
                "Carlos Updated",
                "88888888888",
                "carlos@email.com",
                "7777777777",
                Set.of(teacher.getId())
        );

        mockMvc.perform(put("/students/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Carlos Updated")))
                .andExpect(jsonPath("$.teachers", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetStudentById() throws Exception {
        Student student = studentRepository.save(
                Student.builder()
                        .name("Carlos")
                        .email("carlos@email.com")
                        .nationalId("88888888888")
                        .phone("7777777777")
                        .teachers(Set.of())
                        .build()
        );

        mockMvc.perform(get("/students/" + student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Carlos")))
                .andExpect(jsonPath("$.nationalId", is("88888888888")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteStudent() throws Exception {
        Student student = studentRepository.save(
                Student.builder()
                        .name("Carlos")
                        .email("carlos@email.com")
                        .nationalId("88888888888")
                        .phone("7777777777")
                        .teachers(Set.of())
                        .build()
        );

        mockMvc.perform(delete("/students/" + student.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNoPermission() throws Exception {
        mockMvc.perform(delete("/students/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateStudentWithTeachers() throws Exception {
        Teacher t1 = teacherRepository.save(Teacher.builder().name("Carlos Silva").phone("11999999999").nationalId("12345678901").build());
        Teacher t2 = teacherRepository.save(Teacher.builder().name("Maria Lima").phone("21988888888").nationalId("09876543210").build());

        StudentDTO dto = new StudentDTO(
                "Joao",
                "11122233344",
                "joao@email.com",
                "11977778888",
                Set.of(t1.getId(), t2.getId())
        );

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Joao"))
                .andExpect(jsonPath("$.teachers", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateStudentWithNewTeachers() throws Exception {
        Teacher t1 = teacherRepository.save(Teacher.builder().name("Carlos").phone("11999999999").nationalId("11111111111").build());
        Teacher t2 = teacherRepository.save(Teacher.builder().name("Marcia").phone("21988888888").nationalId("22222222222").build());
        Teacher t3 = teacherRepository.save(Teacher.builder().name("Joana").phone("21988887777").nationalId("33333333333").build());

        Student student = studentRepository.save(
                Student.builder()
                        .name("Lucia")
                        .nationalId("99988877766")
                        .email("lucia@email.com")
                        .phone("11977778888")
                        .teachers(Set.of(t1, t2))
                        .build()
        );

        StudentDTO updated = new StudentDTO(
                "Lucia Updated",
                "99988877766",
                "lucia_new@email.com",
                "21999999999",
                Set.of(t3.getId())
        );

        mockMvc.perform(put("/students/" + student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lucia Updated"))
                .andExpect(jsonPath("$.teachers", hasSize(1)));
    }

    @Test
    void shouldListStudentsWithTeachers() throws Exception {
        Teacher t1 = teacherRepository.save(Teacher.builder().name("Carlos").phone("11999999999").nationalId("12312312312").build());

        Student student = studentRepository.save(
                Student.builder()
                        .name("Julia")
                        .nationalId("55566677788")
                        .email("julia@email.com")
                        .phone("11988887777")
                        .teachers(Set.of(t1))
                        .build()
        );

        studentRepository.flush();

        mockMvc.perform(get("/students")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Julia"))
                .andExpect(jsonPath("$[0].teachers", hasSize(1)))
                .andExpect(jsonPath("$[0].teachers[0].id").value(t1.getId().intValue()))
                .andExpect(jsonPath("$[0].teachers[0].name").value("Carlos"));
    }
}
