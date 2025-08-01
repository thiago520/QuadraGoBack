package com.quadrago.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrago.backend.dtos.AlunoDTO;
import com.quadrago.backend.models.Aluno;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @BeforeEach
    void setUp() {
        alunoRepository.deleteAll();
        professorRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveSalvarAluno() throws Exception {
        Professor prof = professorRepository.save(
                Professor.builder()
                        .nome("Prof. Teste")
                        .telefone("11999999999")
                        .cpf("12345678901")
                        .build()
        );

        AlunoDTO dto = new AlunoDTO("Joao", "12345678901", "joao@email.com", "11999999999", Set.of(prof.getId()));

        mockMvc.perform(post("/aluno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Joao")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarAluno() throws Exception {
        Professor prof = professorRepository.save(
                Professor.builder()
                        .nome("Prof. Teste")
                        .telefone("11999999999")
                        .cpf("12345678901")
                        .build()
        );

        Aluno aluno = alunoRepository.save(
                Aluno.builder()
                        .nome("Carlos")
                        .email("carlos@email.com")
                        .cpf("88888888888")
                        .telefone("7777777777")
                        .professores(Set.of())
                        .build()
        );

        AlunoDTO dto = new AlunoDTO("Carlos Atualizado", "88888888888", "carlos@email.com", "7777777777", Set.of(prof.getId()));

        mockMvc.perform(put("/aluno/" + aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Carlos Atualizado")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveBuscarAlunoPorId() throws Exception {
        Aluno aluno = alunoRepository.save(
                Aluno.builder()
                        .nome("Carlos")
                        .email("carlos@email.com")
                        .cpf("88888888888")
                        .telefone("7777777777")
                        .professores(Set.of())
                        .build()
        );

        mockMvc.perform(get("/aluno/" + aluno.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Carlos")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveDeletarAluno() throws Exception {
        Aluno aluno = alunoRepository.save(
                Aluno.builder()
                        .nome("Carlos")
                        .email("carlos@email.com")
                        .cpf("88888888888")
                        .telefone("7777777777")
                        .professores(Set.of())
                        .build()
        );

        mockMvc.perform(delete("/aluno/" + aluno.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403_QuandoNaoTemPermissao() throws Exception {
        mockMvc.perform(delete("/aluno/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveSalvarAlunoComProfessores() throws Exception {
        Professor p1 = professorRepository.save(Professor.builder().nome("Carlos Silva").telefone("11999999999").cpf("12345678901").build());
        Professor p2 = professorRepository.save(Professor.builder().nome("Maria Lima").telefone("21988888888").cpf("09876543210").build());

        AlunoDTO dto = new AlunoDTO("João", "11122233344", "joao@email.com", "11977778888", Set.of(p1.getId(), p2.getId()));

        mockMvc.perform(post("/aluno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.professores", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarAlunoComNovosProfessores() throws Exception {
        Professor p1 = professorRepository.save(Professor.builder().nome("Carlos").telefone("11999999999").cpf("11111111111").build());
        Professor p2 = professorRepository.save(Professor.builder().nome("Marcia").telefone("21988888888").cpf("22222222222").build());
        Professor p3 = professorRepository.save(Professor.builder().nome("Joana").telefone("21988887777").cpf("33333333333").build());

        Aluno aluno = alunoRepository.save(
                Aluno.builder()
                        .nome("Lucia")
                        .cpf("99988877766")
                        .email("lucia@email.com")
                        .telefone("11977778888")
                        .professores(Set.of(p1, p2))
                        .build()
        );

        AlunoDTO atualizado = new AlunoDTO("Lucia Atualizada", "99988877766", "lucia_nova@email.com", "21999999999", Set.of(p3.getId()));

        mockMvc.perform(put("/aluno/" + aluno.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lucia Atualizada"))
                .andExpect(jsonPath("$.professores", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRemoverAlunoComProfessores() throws Exception {
        Professor p1 = professorRepository.save(Professor.builder().nome("Carlos").telefone("11999999999").cpf("12312312312").build());

        Aluno aluno = alunoRepository.save(
                Aluno.builder()
                        .nome("Pedro")
                        .cpf("88877766655")
                        .email("pedro@email.com")
                        .telefone("11977779999")
                        .professores(Set.of(p1))
                        .build()
        );

        mockMvc.perform(delete("/aluno/" + aluno.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveListarAlunosComProfessores() throws Exception {
        Professor p1 = professorRepository.save(Professor.builder().nome("Carlos").telefone("11999999999").cpf("12312312312").build());

        Aluno aluno = alunoRepository.save(
                Aluno.builder()
                        .nome("Julia")
                        .cpf("55566677788")
                        .email("julia@email.com")
                        .telefone("11988887777")
                        .professores(Set.of(p1))
                        .build()
        );

        alunoRepository.flush();

        mockMvc.perform(get("/aluno")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Julia"))
                .andExpect(jsonPath("$[0].professores", hasSize(1)))
                .andExpect(jsonPath("$[0].professores[0].id").value(p1.getId().intValue()))
                .andExpect(jsonPath("$[0].professores[0].nome").value("Carlos"));
    }
}
