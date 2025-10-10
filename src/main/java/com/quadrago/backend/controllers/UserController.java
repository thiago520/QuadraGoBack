package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.UserDtos.*;
import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.Person;
import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.PersonRepository;
import com.quadrago.backend.repositories.StudentProfileRepository;
import com.quadrago.backend.repositories.TeacherProfileRepository;
import com.quadrago.backend.repositories.UserRepository;
import com.quadrago.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final PersonRepository personRepo;
    private final TeacherProfileRepository teacherRepo;
    private final StudentProfileRepository studentRepo;

    /* ========= CREATE ========= */

    /**
     * Cadastro de usuário PF é público
     */
    @PreAuthorize("permitAll()")
    @PostMapping("/person")
    public ResponseEntity<UserDetailsResponse> createPerson(@RequestBody @Valid CreatePersonUserRequest req) {
        var basic = userService.createPersonUser(req);
        var details = toDetails(loadUserOrThrow(basic.id()));
        return ResponseEntity.created(URI.create("/users/" + details.id())).body(details);
    }

    /* ========= READ ========= */

    /**
     * Admin ou o próprio usuário podem consultar
     */
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toDetails(loadUserOrThrow(id)));
    }

    /**
     * Sem filtro (lista): ADMIN apenas.
     * Com ?email=: ADMIN ou o próprio usuário (comparação por e-mail).
     */
    @PreAuthorize("#email == null ? hasRole('ADMIN') : (hasRole('ADMIN') or #email?.toLowerCase() == principal?.username?.toLowerCase())")
    @GetMapping
    public ResponseEntity<?> listOrFind(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return userService.getByEmail(email)
                    .<ResponseEntity<?>>map(r -> ResponseEntity.ok(toDetails(loadUserOrThrow(r.id()))))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        List<UserResponse> list = userService.listAll();
        return ResponseEntity.ok(list);
    }

    /* ========= UPDATE - USER ========= */

    /**
     * Trocar e-mail: admin ou dono
     */
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}/email")
    public ResponseEntity<UserDetailsResponse> updateEmail(@PathVariable Long id, @RequestBody @Valid UpdateEmailRequest req) {
        userService.updateEmail(id, req.email());
        return ResponseEntity.ok(toDetails(loadUserOrThrow(id)));
    }

    /**
     * Trocar senha: admin ou dono
     */
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestBody @Valid UpdatePasswordRequest req) {
        userService.updatePassword(id, req.password());
        return ResponseEntity.noContent().build();
    }

    /**
     * Status: apenas admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<UserDetailsResponse> updateStatus(@PathVariable Long id, @RequestBody @Valid UpdateStatusRequest req) {
        userService.updateStatus(id, req.status());
        return ResponseEntity.ok(toDetails(loadUserOrThrow(id)));
    }

    /**
     * Dados pessoais: admin ou dono
     */
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}/person")
    public ResponseEntity<UserDetailsResponse> updatePerson(@PathVariable Long id, @RequestBody @Valid UpdatePersonDataRequest req) {
        userService.updatePersonData(id, req.name(), req.cpf(), req.phone(), req.birthDate());
        return ResponseEntity.ok(toDetails(loadUserOrThrow(id)));
    }

    /* ========= ROLES (RBAC) ========= */

    /**
     * Ver papéis: admin ou dono
     */
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<RoleName>> getRoles(@PathVariable Long id) {
        var user = loadUserOrThrow(id);
        var roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        return ResponseEntity.ok(roles);
    }

    /**
     * Gerenciar papéis: apenas admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles/add")
    public ResponseEntity<Set<RoleName>> addRoles(@PathVariable Long id, @RequestBody @Valid RolesRequest req) {
        return ResponseEntity.ok(userService.addRoles(id, req.roles()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles/remove")
    public ResponseEntity<Set<RoleName>> removeRoles(@PathVariable Long id, @RequestBody @Valid RolesRequest req) {
        return ResponseEntity.ok(userService.removeRoles(id, req.roles()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles/replace")
    public ResponseEntity<Set<RoleName>> replaceRoles(@PathVariable Long id, @RequestBody @Valid RolesRequest req) {
        return ResponseEntity.ok(userService.replaceRoles(id, req.roles()));
    }

    /* ========= DELETE ========= */

    /**
     * Deletar conta: admin ou dono
     */
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /* ========= HELPERS ========= */

    private User loadUserOrThrow(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    private UserDetailsResponse toDetails(User u) {
        var partyId = u.getParty().getId();
        Person p = personRepo.findById(partyId)
                .orElseThrow(() -> new IllegalStateException("Person não encontrado para party " + partyId));

        boolean hasTeacher = teacherRepo.existsByUserId(u.getId());
        boolean hasStudent = studentRepo.existsByUserId(u.getId());

        var roles = u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());

        return new UserDetailsResponse(
                u.getId(),
                u.getEmail(),
                p.getName(),
                u.getStatus(),
                roles,
                hasTeacher,
                hasStudent
        );
    }
}
