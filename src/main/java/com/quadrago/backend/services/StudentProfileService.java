package com.quadrago.backend.services;

import com.quadrago.backend.dtos.UserDtos.CreateStudentProfileRequest;
import com.quadrago.backend.dtos.UserDtos.UpdateStudentProfileRequest;
import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.StudentProfile;
import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.StudentProfileRepository;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final UserRepository userRepo;
    private final StudentProfileRepository studentRepo;
    private final RoleService roleService;

    /* ===================== CREATE ===================== */

    @Transactional
    public Long create(Long userId, CreateStudentProfileRequest req) {
        User user = fetchUser(userId);
        ensureStudentRole(user, true);          // concede STUDENT se não tiver
        assertProfileNotExists(userId);

        StudentProfile sp = StudentProfile.builder()
                .user(user)
                .emergencyContact(normalize(req.emergencyContact()))
                .notes(normalize(req.notes()))
                .build();

        return studentRepo.save(sp).getUserId();
    }

    /* ====================== READ ====================== */

    @Transactional(readOnly = true)
    public StudentProfile get(Long userId) {
        return studentRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de aluno não encontrado"));
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public StudentProfile update(Long userId, UpdateStudentProfileRequest req) {
        StudentProfile sp = get(userId);
        if (req.emergencyContact() != null) sp.setEmergencyContact(normalize(req.emergencyContact()));
        if (req.notes() != null) sp.setNotes(normalize(req.notes()));
        return studentRepo.save(sp);
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public void delete(Long userId) {
        if (!studentRepo.existsByUserId(userId)) {
            throw new IllegalArgumentException("Perfil de aluno não encontrado");
        }
        studentRepo.deleteById(userId);
        // Observação: não removemos o ROLE_STUDENT automaticamente.
    }

    /* ===================== HELPERS ===================== */

    private User fetchUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    private void ensureStudentRole(User user, boolean grantIfMissing) {
        boolean hasRole = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.STUDENT);
        if (!hasRole && grantIfMissing) {
            user.getRoles().add(roleService.getOrCreate(RoleName.STUDENT));
        } else if (!hasRole) {
            throw new IllegalStateException("Usuário não possui o papel STUDENT");
        }
    }

    private void assertProfileNotExists(Long userId) {
        if (studentRepo.existsByUserId(userId)) {
            throw new IllegalStateException("Perfil de aluno já existe");
        }
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }
}
