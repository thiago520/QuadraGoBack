package com.quadrago.backend.config;

import com.quadrago.backend.dtos.UserDtos.CreatePersonUserRequest;
import com.quadrago.backend.dtos.UserDtos.CreateStudentProfileRequest;
import com.quadrago.backend.dtos.UserDtos.CreateTeacherProfileRequest;
import com.quadrago.backend.enums.RoleName;
import com.quadrago.backend.services.RoleService;
import com.quadrago.backend.services.StudentProfileService;
import com.quadrago.backend.services.TeacherProfileService;
import com.quadrago.backend.services.UserService;
import com.quadrago.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Component
@Profile({"dev", "default"}) // ajuste conforme necessidade (ex.: incluir "test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;
    private final UserRepository userRepo;
    private final TeacherProfileService teacherProfileService;
    private final StudentProfileService studentProfileService;

    /**
     * Liga/desliga criação do admin
     */
    @Value("${app.seed.admin.enabled:true}")
    private boolean seedAdminEnabled;

    @Value("${app.seed.admin.email:admin@quadrago.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:admin}")
    private String adminPassword;

    /**
     * Liga/desliga criação de usuários demo (teacher/student)
     */
    @Value("${app.seed.demo.enabled:false}")
    private boolean seedDemoEnabled;

    @Override
    public void run(String... args) {
        seedRoles();
        if (seedAdminEnabled) seedAdmin();
        if (seedDemoEnabled) seedDemoUsers();
    }

    /* ===================== SEEDS ===================== */

    private void seedRoles() {
        for (RoleName rn : RoleName.values()) {
            roleService.getOrCreate(rn);
        }
        log.info("Roles garantidos: {}", (Object) RoleName.values());
    }

    private void seedAdmin() {
        if (userRepo.findByEmail(adminEmail.toLowerCase()).isPresent()) {
            log.info("Admin já existe ({}) — ignorando", adminEmail);
            return;
        }

        var req = new CreatePersonUserRequest(
                "Administrador",
                null,                 // cpf
                null,                 // phone
                null,                 // birthDate
                adminEmail,
                adminPassword,        // use uma senha segura em produção
                Set.of(RoleName.ADMIN)
        );

        var created = userService.createPersonUser(req);
        log.info("Admin criado: {} (id={})", created.email(), created.id());
    }

    private void seedDemoUsers() {
        // ===== Demo Teacher =====
        final String teacherEmail = "teacher@quadrago.local";
        Long teacherId = userRepo.findByEmail(teacherEmail)
                .map(u -> u.getId())
                .orElseGet(() -> {
                    var req = new CreatePersonUserRequest(
                            "Professor Demo",
                            "11122233344",
                            "11999990000",
                            LocalDate.of(1990, 1, 1),
                            teacherEmail,
                            "teacher",
                            Set.of(RoleName.TEACHER)
                    );
                    var created = userService.createPersonUser(req);
                    log.info("Usuário teacher criado: {} (id={})", created.email(), created.id());
                    return created.id();
                });

        // cria/garante perfil de professor
        try {
            teacherProfileService.create(teacherId, new CreateTeacherProfileRequest(
                    "Instrutor de esportes demo",
                    new BigDecimal("120.00"),
                    Set.of("Futebol", "Vôlei")
            ));
            log.info("TeacherProfile criado para userId={}", teacherId);
        } catch (IllegalStateException e) {
            log.info("TeacherProfile já existia para userId={}", teacherId);
        }

        // ===== Demo Student =====
        final String studentEmail = "student@quadrago.local";
        Long studentId = userRepo.findByEmail(studentEmail)
                .map(u -> u.getId())
                .orElseGet(() -> {
                    var req = new CreatePersonUserRequest(
                            "Aluno Demo",
                            "55566677788",
                            "11888880000",
                            LocalDate.of(2002, 5, 15),
                            studentEmail,
                            "student",
                            Set.of(RoleName.STUDENT)
                    );
                    var created = userService.createPersonUser(req);
                    log.info("Usuário student criado: {} (id={})", created.email(), created.id());
                    return created.id();
                });

        try {
            studentProfileService.create(studentId, new CreateStudentProfileRequest(
                    "Contato de emergência: (11) 98888-0000",
                    "Aluno de teste para cenários de matrícula"
            ));
            log.info("StudentProfile criado para userId={}", studentId);
        } catch (IllegalStateException e) {
            log.info("StudentProfile já existia para userId={}", studentId);
        }
    }
}
