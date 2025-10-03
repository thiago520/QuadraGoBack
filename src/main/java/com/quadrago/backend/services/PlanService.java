package com.quadrago.backend.services;

import com.quadrago.backend.dtos.PlanDTO;
import com.quadrago.backend.dtos.PlanResponseDTO;
import com.quadrago.backend.models.Plan;
import com.quadrago.backend.models.PlanFeature;
import com.quadrago.backend.models.Teacher;
import com.quadrago.backend.repositories.PlanRepository;
import com.quadrago.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public List<PlanResponseDTO> listPublicByTeacher(Long teacherId) {
        return planRepository.findByTeacherIdAndActiveTrueOrderByTitleAsc(teacherId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PlanResponseDTO create(Long teacherId, @Valid PlanDTO dto) {
        Teacher owner = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        // título único por professor (case-insensitive)
        if (planRepository.existsByTeacherIdAndTitleIgnoreCase(teacherId, dto.getTitle().trim())) {
            throw new IllegalArgumentException("Plan title already exists for this teacher");
        }

        Plan plan = Plan.builder()
                .teacher(owner)
                .icon(dto.getIcon().trim())
                .title(dto.getTitle().trim())
                .price(dto.getPrice())
                .period(dto.getPeriod())
                .active(true)
                .build();

        List<PlanFeature> features = buildFeatures(dto);
        plan.clearAndAddFeatures(features);

        Plan saved = planRepository.save(plan);
        log.info("Plan created: id={}, teacherId={}, title='{}'", saved.getId(), teacherId, saved.getTitle());
        return toResponse(saved);
    }

    @Transactional
    public PlanResponseDTO update(Long teacherId, Long planId, @Valid PlanDTO dto) {
        Plan plan = planRepository.findByIdAndTeacherId(planId, teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        // título único por professor (case-insensitive), ignorando o próprio id
        if (planRepository.existsByTeacherIdAndTitleIgnoreCaseAndIdNot(teacherId, dto.getTitle().trim(), planId)) {
            throw new IllegalArgumentException("Plan title already exists for this teacher");
        }

        plan.setIcon(dto.getIcon().trim());
        plan.setTitle(dto.getTitle().trim());
        plan.setPrice(dto.getPrice());
        plan.setPeriod(dto.getPeriod());

        List<PlanFeature> features = buildFeatures(dto);
        plan.clearAndAddFeatures(features);

        Plan updated = planRepository.save(plan);
        log.info("Plan updated: id={}, teacherId={}, title='{}'", updated.getId(), teacherId, updated.getTitle());
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long teacherId, Long planId) {
        Plan plan = planRepository.findByIdAndTeacherId(planId, teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));
        planRepository.delete(plan);
        log.warn("Plan deleted: id={}, teacherId={}", planId, teacherId);
    }

    private List<PlanFeature> buildFeatures(PlanDTO dto) {
        // mantém a ordem recebida do frontend como sortOrder (0..n)
        return dto.getFeatures().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(str -> PlanFeature.builder().text(str).sortOrder(0).build())
                .map(new java.util.function.Function<PlanFeature, PlanFeature>() {
                    int idx = 0;
                    @Override public PlanFeature apply(PlanFeature f) {
                        f.setSortOrder(idx++);
                        return f;
                    }
                })
                .toList();
    }

    private PlanResponseDTO toResponse(Plan p) {
        return PlanResponseDTO.builder()
                .id(p.getId())
                .teacherId(p.getTeacher().getId())
                .icon(p.getIcon())
                .title(p.getTitle())
                .price(p.getPrice())
                .period(p.getPeriod())
                .active(p.isActive())
                .features(p.getFeatures().stream()
                        .sorted(Comparator.comparingInt(PlanFeature::getSortOrder).thenComparing(PlanFeature::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(PlanFeature::getText)
                        .toList())
                .build();
    }
}
