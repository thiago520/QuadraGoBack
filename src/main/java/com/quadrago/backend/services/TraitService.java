package com.quadrago.backend.services;

import com.quadrago.backend.dtos.TraitDTO;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.models.Trait;
import com.quadrago.backend.repositories.TeacherRepository;
import com.quadrago.backend.repositories.TraitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraitService {

    private final TraitRepository traitRepository;
    private final TeacherRepository teacherRepository;

    /* ===================== CREATE ===================== */

    @Transactional
    public TraitDTO create(TraitDTO dto) {
        Long teacherId = dto.getTeacherId();
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found: id=" + teacherId));

        // opcional: impedir nomes duplicados por teacher (case-insensitive)
        if (traitRepository.existsByTeacher_IdAndNameIgnoreCase(teacherId, dto.getName())) {
            log.warn("Trait duplicate for teacherId={}, name='{}'", teacherId, dto.getName());
            throw new IllegalArgumentException("trait name already exists for this teacher");
        }

        Trait trait = Trait.builder()
                .name(dto.getName())
                .teacher(teacher)
                .build();

        Trait saved = traitRepository.save(trait);
        log.info("Trait created: id={}, name='{}', teacherId={}", saved.getId(), saved.getName(), teacherId);
        return new TraitDTO(saved);
    }

    /* ===================== UPDATE ===================== */

    /* ===================== UPDATE ===================== */

    @Transactional
    public TraitDTO update(Long id, TraitDTO dto) {
        Trait existing = traitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trait not found: id=" + id));

        // teacher alvo: do DTO (se vier) ou o atual
        Long targetTeacherId = (dto.getTeacherId() != null)
                ? dto.getTeacherId()
                : existing.getTeacher().getId();

        // >>> Sempre validar/buscar o teacher (isso satisfaz o teste que espera a invocação) <<<
        Teacher targetTeacher = teacherRepository.findById(targetTeacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found: id=" + targetTeacherId));

        // capture o estado ANTES de alterar o objeto para calcular corretamente
        boolean teacherChanged = !existing.getTeacher().getId().equals(targetTeacherId);

        String newName = (dto.getName() != null) ? dto.getName() : existing.getName();
        boolean nameChanged = !newName.equalsIgnoreCase(existing.getName());

        // validar duplicidade se name ou teacher mudaram
        if (teacherChanged || nameChanged) {
            if (traitRepository.existsByTeacher_IdAndNameIgnoreCase(targetTeacherId, newName)) {
                log.warn("Trait duplicate on update for teacherId={}, name='{}'", targetTeacherId, newName);
                throw new IllegalArgumentException("trait name already exists for this teacher");
            }
        }

        if (teacherChanged) {
            existing.setTeacher(targetTeacher);
        }
        existing.setName(newName);

        Trait saved = traitRepository.save(existing);
        log.info("Trait updated: id={}, name='{}', teacherId={}", saved.getId(), saved.getName(), saved.getTeacher().getId());
        return new TraitDTO(saved);
    }


    /* ===================== READ ===================== */

    @Transactional(readOnly = true)
    public List<TraitDTO> listByTeacher(Long teacherId) {
        List<TraitDTO> list = traitRepository.findByTeacher_Id(teacherId).stream()
                .map(TraitDTO::new)
                .toList();
        log.debug("Listed {} traits for teacherId={}", list.size(), teacherId);
        return list;
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public boolean delete(Long id) {
        return traitRepository.findById(id).map(tr -> {
            log.warn("Deleting trait id={}", id);
            traitRepository.delete(tr);
            log.info("Trait deleted id={}", id);
            return true;
        }).orElse(false);
    }
}
