package com.quadrago.backend.services;

import com.quadrago.backend.dtos.UserDtos.CreateTeacherProfileRequest;
import com.quadrago.backend.dtos.UserDtos.UpdateTeacherProfileRequest;
import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.models.TeacherProfile;
import com.quadrago.backend.models.User;
import com.quadrago.backend.repositories.TeacherProfileRepository;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherProfileService {

    private final UserRepository userRepo;
    private final TeacherProfileRepository teacherRepo;
    private final RoleService roleService;

    /* ===================== CREATE ===================== */

    @Transactional
    public Long create(Long userId, CreateTeacherProfileRequest req) {
        User user = fetchUser(userId);
        ensureTeacherRole(user, true);          // concede TEACHER se não tiver
        assertProfileNotExists(userId);

        validateHourly(req.hourlyRate());

        TeacherProfile tp = TeacherProfile.builder()
                .user(user)
                .bio(req.bio())
                .hourlyRate(req.hourlyRate())
                .specialties(sanitize(req.specialties()))
                .build();

        return teacherRepo.save(tp).getUserId();
    }

    /* ====================== READ ====================== */

    @Transactional(readOnly = true)
    public TeacherProfile get(Long userId) {
        return teacherRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de professor não encontrado"));
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public TeacherProfile update(Long userId, UpdateTeacherProfileRequest req) {
        TeacherProfile tp = get(userId);

        if (req.bio() != null) tp.setBio(req.bio());
        if (req.hourlyRate() != null) {
            validateHourly(req.hourlyRate());
            tp.setHourlyRate(req.hourlyRate());
        }
        if (req.specialties() != null) {
            tp.getSpecialties().clear();
            tp.getSpecialties().addAll(sanitize(req.specialties()));
        }
        return teacherRepo.save(tp);
    }

    @Transactional
    public void delete(Long userId) {
        if (!teacherRepo.existsByUserId(userId)) {
            throw new IllegalArgumentException("Perfil de professor não encontrado");
        }
        teacherRepo.deleteById(userId);
        // Observação: não removemos o ROLE_TEACHER automaticamente.
    }

    /* ========== Ops convenientes sobre especialidades ========== */

    @Transactional
    public Set<String> addSpecialties(Long userId, Set<String> specialties) {
        TeacherProfile tp = get(userId);
        tp.getSpecialties().addAll(sanitize(specialties));
        teacherRepo.save(tp);
        return new LinkedHashSet<>(tp.getSpecialties());
    }

    @Transactional
    public Set<String> removeSpecialties(Long userId, Set<String> specialties) {
        TeacherProfile tp = get(userId);
        if (specialties != null) {
            Set<String> toRemove = sanitize(specialties);
            tp.getSpecialties().removeIf(s -> toRemove.contains(normalize(s)));
            teacherRepo.save(tp);
        }
        return new LinkedHashSet<>(tp.getSpecialties());
    }

    @Transactional
    public Set<String> replaceSpecialties(Long userId, Set<String> specialties) {
        TeacherProfile tp = get(userId);
        tp.getSpecialties().clear();
        tp.getSpecialties().addAll(sanitize(specialties));
        teacherRepo.save(tp);
        return new LinkedHashSet<>(tp.getSpecialties());
    }

    /* ===================== HELPERS ===================== */

    private User fetchUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    private void ensureTeacherRole(User user, boolean grantIfMissing) {
        boolean hasRole = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.TEACHER);
        if (!hasRole && grantIfMissing) {
            user.getRoles().add(roleService.getOrCreate(RoleName.TEACHER));
        } else if (!hasRole) {
            throw new IllegalStateException("Usuário não possui o papel TEACHER");
        }
    }

    private void assertProfileNotExists(Long userId) {
        if (teacherRepo.existsByUserId(userId)) {
            throw new IllegalStateException("Perfil de professor já existe");
        }
    }

    private void validateHourly(BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException("hourlyRate não pode ser negativo");
        }
    }

    private Set<String> sanitize(Set<String> in) {
        if (in == null) return new LinkedHashSet<>();
        return in.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String s) {
        String t = s.trim();
        // escolha simples: manter case original; apenas trim.
        // Se quiser case-insensitive, use: t.toLowerCase(Locale.ROOT)
        return t;
    }
}
