package com.quadrago.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quadrago.backend.dtos.AlunoDTO;
import com.quadrago.backend.dtos.ProfessorDTO;
import com.quadrago.backend.models.Professor;
import com.quadrago.backend.repositories.AlunoRepository;
import com.quadrago.backend.repositories.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfessorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    private Long professorId;

    @BeforeEach
    void setup() {
        alunoRepository.deleteAll();
        professorRepository.deleteAll();

        Professor p = Professor.builder()
                .nome("Carlos Silva")
                .telefone("11987654321")
                .cpf("12345678901")
                .alunos(Collections.emptySet())
                .caracteristicas(new HashSet<>())
                .build();
        p = professorRepository.save(p);
        professorId = p.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveListarProfessores() throws Exception {
        mockMvc.perform(get("/professor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome", is("Carlos Silva")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveBuscarProfessorPorId() throws Exception {
        mockMvc.perform(get("/professor/{id}", professorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Carlos Silva")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarProfessor() throws Exception {
        ProfessorDTO dto = new ProfessorDTO();
        dto.setNome("João da Silva");
        dto.setTelefone("11988887777");
        dto.setCpf("12345678901");

        mockMvc.perform(post("/professor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("João da Silva")))
                .andExpect(jsonPath("$.telefone", is("11988887777")))
                .andExpect(jsonPath("$.cpf", is("12345678901")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarProfessor() throws Exception {
        ProfessorDTO dto = new ProfessorDTO();
        dto.setNome("Nome Atualizado");
        dto.setTelefone("11977776666");
        dto.setCpf("99988877766");

        mockMvc.perform(put("/professor/{id}", professorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nome Atualizado")))
                .andExpect(jsonPath("$.telefone", is("11977776666")))
                .andExpect(jsonPath("$.cpf", is("99988877766")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveDeletarProfessor() throws Exception {
        mockMvc.perform(delete("/professor/{id}", professorId))
                .andExpect(status().isNoContent());

        Optional<Professor> excluido = professorRepository.findById(professorId);
        assertTrue(excluido.isEmpty(), "Professor deveria ter sido deletado");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404_QuandoProfessorNaoExistir() throws Exception {
        mockMvc.perform(get("/professor/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400_QuandoCriarProfessorComDadosInvalidos() throws Exception {
        ProfessorDTO dto = new ProfessorDTO(); // vazio

        mockMvc.perform(post("/professor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", not(empty())));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403_QuandoUsuarioNaoTemPermissaoParaDeletar() throws Exception {
        mockMvc.perform(delete("/professor/{id}", professorId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403_QuandoUsuarioNaoTemPermissao() throws Exception {
        mockMvc.perform(get("/professor"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar401_QuandoNaoAutenticado() throws Exception {
        mockMvc.perform(delete("/professor/{id}", professorId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveSalvarAlunoComProfessores() throws Exception {
        Professor professor1 = professorRepository.save(Professor.builder()
                .nome("Carlos Silva")
                .telefone("11999999999")
                .cpf("12345678901")
                .alunos(new HashSet<>())
                .caracteristicas(new HashSet<>())
                .build());

        Professor professor2 = professorRepository.save(Professor.builder()
                .nome("Maria Lima")
                .telefone("21988888888")
                .cpf("09876543210")
                .alunos(new HashSet<>())
                .caracteristicas(new HashSet<>())
                .build());

        AlunoDTO alunoDTO = AlunoDTO.builder()
                .nome("João")
                .cpf("11122233344")
                .email("joao@email.com")
                .telefone("11977778888")
                .professoresIds(Set.of(professor1.getId(), professor2.getId()))
                .build();

        mockMvc.perform(post("/aluno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alunoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.professores.length()").value(2));
    }
}
