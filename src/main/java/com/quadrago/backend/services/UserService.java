package com.quadrago.backend.services;

import com.quadrago.backend.dtos.UserDtos.CreatePersonUserRequest;
import com.quadrago.backend.dtos.UserDtos.UserResponse;
import com.quadrago.backend.enums.PartyType;
import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.enums.UserStatus;
import com.quadrago.backend.models.*;
import com.quadrago.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PartyRepository partyRepo;
    private final PersonRepository personRepo;
    private final UserRepository userRepo;
    private final TeacherProfileRepository teacherProfileRepo;
    private final StudentProfileRepository studentProfileRepo;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder; // configure no SecurityConfig

  /* ==============================
     CREATE
     ============================== */

    @Transactional
    public UserResponse createPersonUser(CreatePersonUserRequest req) {
        if (userRepo.existsByEmail(req.email().toLowerCase())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        if (req.cpf() != null) {
            personRepo.findByCpf(req.cpf()).ifPresent(p -> {
                throw new IllegalArgumentException("CPF já cadastrado");
            });
        }

        Party party = partyRepo.save(Party.builder().partyType(PartyType.PERSON).build());
        Person person = personRepo.save(Person.builder()
                .party(party).name(req.name()).cpf(req.cpf()).phone(req.phone()).birthDate(req.birthDate())
                .build());

        // mantém 'user' sem reatribuir dentro de lambda
        User user = User.builder()
                .party(party)
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();

        // resolve roles antes e só adiciona no user fora da lambda
        Set<RoleName> roles = (req.roles() == null || req.roles().isEmpty())
                ? Set.of(RoleName.STUDENT) : req.roles();

        var attachedRoles = roles.stream()
                .map(roleService::getOrCreate)
                .collect(java.util.stream.Collectors.toSet());

        user.getRoles().addAll(attachedRoles);

        User saved = userRepo.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), person.getName());
    }


  /* ==============================
     READ
     ============================== */

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        Person person = getPersonOf(user);
        return new UserResponse(user.getId(), user.getEmail(), person.getName());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getByEmail(String email) {
        return userRepo.findByEmail(email.toLowerCase())
                .map(u -> {
                    Person p = getPersonOf(u);
                    return new UserResponse(u.getId(), u.getEmail(), p.getName());
                });
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return userRepo.findAll().stream().map(u -> {
            Person p = getPersonOf(u);
            return new UserResponse(u.getId(), u.getEmail(), p.getName());
        }).collect(Collectors.toList());
    }

  /* ==============================
     UPDATE (dados de login/conta)
     ============================== */

    /**
     * Atualiza o e-mail (mantém case-insensitive único).
     */
    @Transactional
    public UserResponse updateEmail(Long userId, String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Novo e-mail é obrigatório");
        }
        String normalized = newEmail.toLowerCase();
        userRepo.findByEmail(normalized).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), userId)) {
                throw new IllegalArgumentException("E-mail já em uso");
            }
        });

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setEmail(normalized);
        Person p = getPersonOf(user);
        return new UserResponse(user.getId(), user.getEmail(), p.getName());
    }

    /**
     * Atualiza a senha (codifica com PasswordEncoder).
     */
    @Transactional
    public void updatePassword(Long userId, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
    }

    /**
     * Atualiza o status do usuário (ACTIVE/INACTIVE/BLOCKED).
     */
    @Transactional
    public void updateStatus(Long userId, UserStatus status) {
        if (status == null) throw new IllegalArgumentException("Status inválido");
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setStatus(status);
    }

  /* ==============================
     UPDATE (dados pessoais - Person)
     Todos os campos são opcionais: se vier null, mantém o valor atual.
     ============================== */

    @Transactional
    public UserResponse updatePersonData(Long userId,
                                         String name,
                                         String cpf,
                                         String phone,
                                         LocalDate birthDate) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Person person = getPersonOf(user);

        if (cpf != null && !Objects.equals(cpf, person.getCpf())) {
            personRepo.findByCpf(cpf).ifPresent(other -> {
                // Se já existe outro Person com esse CPF, impede.
                if (!Objects.equals(other.getPartyId(), person.getPartyId())) {
                    throw new IllegalArgumentException("CPF já cadastrado");
                }
            });
            person.setCpf(cpf);
        }
        if (name != null) person.setName(name);
        if (phone != null) person.setPhone(phone);
        if (birthDate != null) person.setBirthDate(birthDate);

        // Retorna resposta atualizada
        return new UserResponse(user.getId(), user.getEmail(), person.getName());
    }

  /* ==============================
     ROLES (RBAC)
     ============================== */

    /**
     * Adiciona papéis ao usuário (idempotente).
     */
    @Transactional
    public Set<RoleName> addRoles(Long userId, Set<RoleName> rolesToAdd) {
        if (rolesToAdd == null || rolesToAdd.isEmpty()) return getRoleNames(userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        rolesToAdd.forEach(rn -> user.getRoles().add(roleService.getOrCreate(rn)));
        return getRoleNames(user);
    }

    /**
     * Remove papéis do usuário (ignora os que ele não tiver).
     */
    @Transactional
    public Set<RoleName> removeRoles(Long userId, Set<RoleName> rolesToRemove) {
        if (rolesToRemove == null || rolesToRemove.isEmpty()) return getRoleNames(userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.getRoles().removeIf(r -> rolesToRemove.contains(r.getName()));
        return getRoleNames(user);
    }

    /**
     * Substitui todos os papéis pelo conjunto informado.
     */
    @Transactional
    public Set<RoleName> replaceRoles(Long userId, Set<RoleName> newRoles) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.getRoles().clear();
        if (newRoles != null && !newRoles.isEmpty()) {
            newRoles.forEach(rn -> user.getRoles().add(roleService.getOrCreate(rn)));
        }
        return getRoleNames(user);
    }

  /* ==============================
     DELETE
     ============================== */

    /**
     * Exclui o usuário e seus perfis. Em seguida remove a Party (e Person por cascata).
     * Ordem:
     * 1) Apaga perfis (teacher/student)
     * 2) Apaga usuário
     * 3) Apaga person/party
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Long partyId = user.getParty().getId();

        // Perfis (se existirem)
        if (teacherProfileRepo.existsByUserId(userId)) {
            teacherProfileRepo.deleteById(userId);
        }
        if (studentProfileRepo.existsByUserId(userId)) {
            studentProfileRepo.deleteById(userId);
        }

        // Remove papéis (não é obrigatório, mas evita linhas supérfluas no join table antes do delete)
        user.getRoles().clear();

        // Apaga usuário
        userRepo.delete(user);

        // Apaga person (se existir) e depois party
        personRepo.findById(partyId).ifPresent(personRepo::delete);
        partyRepo.findById(partyId).ifPresent(partyRepo::delete);
    }

  /* ==============================
     HELPERS
     ============================== */

    private Person getPersonOf(User user) {
        Long partyId = user.getParty().getId();
        return personRepo.findById(partyId)
                .orElseThrow(() -> new IllegalStateException("Person não encontrado para party " + partyId));
    }

    private Set<RoleName> getRoleNames(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return getRoleNames(user);
    }

    private Set<RoleName> getRoleNames(User user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }
}
