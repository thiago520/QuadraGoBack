package com.quadrago.backend.controllers;

import com.quadrago.backend.dtos.UserDtos.CreateStudentProfileRequest;
import com.quadrago.backend.dtos.UserDtos.CreateTeacherProfileRequest;
import com.quadrago.backend.dtos.UserDtos.StudentProfileResponse;
import com.quadrago.backend.dtos.UserDtos.TeacherProfileResponse;
import com.quadrago.backend.dtos.UserDtos.UpdateStudentProfileRequest;
import com.quadrago.backend.dtos.UserDtos.UpdateTeacherProfileRequest;
import com.quadrago.backend.models.StudentProfile;
import com.quadrago.backend.models.TeacherProfile;
import com.quadrago.backend.repositories.StudentProfileRepository;
import com.quadrago.backend.repositories.TeacherProfileRepository;
import com.quadrago.backend.repositories.UserRepository;
import com.quadrago.backend.services.StudentProfileService;
import com.quadrago.backend.services.TeacherProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final TeacherProfileService teacherService;
    private final StudentProfileService studentService;

    private final TeacherProfileRepository teacherRepo;   // ainda usado em flags externas, pode remover se não precisar
    private final StudentProfileRepository studentRepo;   // idem
    private final UserRepository userRepo;

    /* ======= TEACHER ======= */

    /**
     * Perfil de professor pode ser consultado publicamente (catálogo).
     */
    @PreAuthorize("permitAll()")
    @GetMapping("/teacher/{userId}")
    public ResponseEntity<TeacherProfileResponse> getTeacher(@PathVariable Long userId) {
        TeacherProfile tp = teacherService.get(userId);
        return ResponseEntity.ok(new TeacherProfileResponse(tp.getUserId(), tp.getBio(), tp.getHourlyRate(), tp.getSpecialties()));
    }

    /**
     * Criar perfil de professor: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PostMapping("/{userId}/teacher")
    public ResponseEntity<Long> createTeacher(@PathVariable Long userId, @RequestBody @Valid CreateTeacherProfileRequest req) {
        ensureUserExists(userId);
        Long id = teacherService.create(userId, req);
        return ResponseEntity.created(URI.create("/profiles/teacher/" + id)).body(id);
    }

    /**
     * Atualizar perfil de professor: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PutMapping("/teacher/{userId}")
    public ResponseEntity<TeacherProfileResponse> updateTeacher(@PathVariable Long userId, @RequestBody @Valid UpdateTeacherProfileRequest req) {
        TeacherProfile tp = teacherService.update(userId, req);
        return ResponseEntity.ok(new TeacherProfileResponse(tp.getUserId(), tp.getBio(), tp.getHourlyRate(), tp.getSpecialties()));
    }

    /**
     * Excluir perfil de professor: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @DeleteMapping("/teacher/{userId}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long userId) {
        teacherService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gerenciar especialidades: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PutMapping("/teacher/{userId}/specialties/add")
    public ResponseEntity<Set<String>> addTeacherSpecialties(@PathVariable Long userId, @RequestBody Set<String> specialties) {
        return ResponseEntity.ok(teacherService.addSpecialties(userId, specialties));
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PutMapping("/teacher/{userId}/specialties/remove")
    public ResponseEntity<Set<String>> removeTeacherSpecialties(@PathVariable Long userId, @RequestBody Set<String> specialties) {
        return ResponseEntity.ok(teacherService.removeSpecialties(userId, specialties));
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PutMapping("/teacher/{userId}/specialties/replace")
    public ResponseEntity<Set<String>> replaceTeacherSpecialties(@PathVariable Long userId, @RequestBody Set<String> specialties) {
        return ResponseEntity.ok(teacherService.replaceSpecialties(userId, specialties));
    }

    /* ======= STUDENT ======= */

    /**
     * Dados de aluno são sensíveis: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @GetMapping("/student/{userId}")
    public ResponseEntity<StudentProfileResponse> getStudent(@PathVariable Long userId) {
        StudentProfile sp = studentService.get(userId);
        return ResponseEntity.ok(new StudentProfileResponse(sp.getUserId(), sp.getEmergencyContact(), sp.getNotes()));
    }

    /**
     * Criar perfil de aluno: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PostMapping("/{userId}/student")
    public ResponseEntity<Long> createStudent(@PathVariable Long userId, @RequestBody @Valid CreateStudentProfileRequest req) {
        ensureUserExists(userId);
        Long id = studentService.create(userId, req);
        return ResponseEntity.created(URI.create("/profiles/student/" + id)).body(id);
    }

    /**
     * Atualizar perfil de aluno: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @PutMapping("/student/{userId}")
    public ResponseEntity<StudentProfileResponse> updateStudent(@PathVariable Long userId, @RequestBody @Valid UpdateStudentProfileRequest req) {
        StudentProfile sp = studentService.update(userId, req);
        return ResponseEntity.ok(new StudentProfileResponse(sp.getUserId(), sp.getEmergencyContact(), sp.getNotes()));
    }

    /**
     * Excluir perfil de aluno: admin ou o próprio usuário
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    @DeleteMapping("/student/{userId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long userId) {
        studentService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    /* ======= HELPERS ======= */

    private void ensureUserExists(Long userId) {
        userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
