package com.quadrago.backend.services;

import com.quadrago.backend.dtos.ClassGroupDTO;
import com.quadrago.backend.dtos.ClassGroupResponseDTO;
import com.quadrago.backend.dtos.ClassScheduleDTO;
import com.quadrago.backend.enums.Level;
import com.quadrago.backend.models.ClassGroup;
import com.quadrago.backend.models.ClassSchedule;
import com.quadrago.backend.models.Student;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.TraitEvaluation;
import com.quadrago.backend.repositories.ClassGroupRepository;
import com.quadrago.backend.repositories.StudentRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    /* ===================== CRUD ===================== */

    @Transactional
    public ClassGroupResponseDTO salvar(ClassGroupDTO dto) {
        log.info("Saving class group: name='{}', teacherId={}, students={}",
                dto.getName(), dto.getTeacherId(), dto.getStudentIds() != null ? dto.getStudentIds().size() : 0);

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found: id=" + dto.getTeacherId()));

        Set<Student> students = dto.getStudentIds() == null || dto.getStudentIds().isEmpty()
                ? Set.of()
                : studentRepository.findAllById(dto.getStudentIds()).stream().collect(Collectors.toSet());

        Set<ClassSchedule> schedules = mapSchedules(dto.getSchedules());

        ClassGroup group = ClassGroup.builder()
                .name(dto.getName())
                .level(dto.getLevel())
                .teacher(teacher)
                .students(students)
                .schedules(schedules)
                .build();

        ClassGroup saved = classGroupRepository.save(group);
        log.info("Class group saved: id={}, name='{}'", saved.getId(), saved.getName());
        return new ClassGroupResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ClassGroupResponseDTO> listar() {
        log.debug("Listing all class groups");
        return classGroupRepository.findAll().stream()
                .map(ClassGroupResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassGroupResponseDTO atualizar(Long id, ClassGroupDTO dto) {
        log.info("Updating class group id={}", id);

        ClassGroup group = classGroupRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Class group not found: id=" + id));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found: id=" + dto.getTeacherId()));

        Set<Student> students = dto.getStudentIds() == null || dto.getStudentIds().isEmpty()
                ? Set.of()
                : studentRepository.findAllById(dto.getStudentIds()).stream().collect(Collectors.toSet());

        Set<ClassSchedule> schedules = mapSchedules(dto.getSchedules());

        group.setName(dto.getName());
        group.setLevel(dto.getLevel());
        group.setTeacher(teacher);
        group.setStudents(students);
        group.setSchedules(schedules);

        ClassGroup updated = classGroupRepository.save(group);
        log.info("Class group updated id={}, name='{}'", updated.getId(), updated.getName());
        return new ClassGroupResponseDTO(updated);
    }

    @Transactional
    public boolean deletar(Long id) {
        log.warn("Deleting class group id={}", id);
        return classGroupRepository.findById(id).map(g -> {
            classGroupRepository.delete(g);
            log.info("Class group deleted id={}", id);
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public ClassGroupResponseDTO buscarPorId(Long id) {
        log.debug("Fetching class group id={}", id);
        return classGroupRepository.findById(id)
                .map(ClassGroupResponseDTO::new)
                .orElseThrow(() -> new NoSuchElementException("Class group not found: id=" + id));
    }

    /* ===================== Business logic ===================== */

    @Transactional(readOnly = true)
    public Optional<Level> calcularNivelTurma(Long classGroupId) {
        log.debug("Calculating computed level for class group id={}", classGroupId);
        return classGroupRepository.findById(classGroupId).map(group -> {
            Set<Student> students = group.getStudents();
            if (students == null || students.isEmpty()) {
                return Level.BEGINNER;
            }

            // média de todas as avaliações (score 0..10)
            List<Integer> allScores = students.stream()
                    .flatMap(st -> st.getTraitEvaluations().stream())
                    .map(te -> te.getScore() != null ? te.getScore() : 0)
                    .toList();

            if (allScores.isEmpty()) return Level.BEGINNER;

            double avg = allScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

            if (avg <= 3)  return Level.BEGINNER;
            if (avg <= 7)  return Level.INTERMEDIATE; // enum atual
            return Level.ADVANCED;
        });
    }

    /* ===================== Helpers ===================== */

    private Set<ClassSchedule> mapSchedules(Set<ClassScheduleDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Set.of();
        return dtos.stream()
                .map(s -> ClassSchedule.of(s.getDayOfWeek(), s.getStartTime(), s.getDuration()))
                .collect(Collectors.toSet());
    }
}
