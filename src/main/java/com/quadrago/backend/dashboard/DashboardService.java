package com.quadrago.backend.dashboard;

import com.quadrago.backend.dashboard.dto.DashboardOverviewDto;
import com.quadrago.backend.dashboard.dto.RecentActivityDto;
import com.quadrago.backend.repositories.LessonRepository;
import com.quadrago.backend.repositories.PaymentRepository;
import com.quadrago.backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LessonRepository lessonRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    private static Instant toInstant(OffsetDateTime odt) {
        return odt != null ? odt.toInstant() : Instant.EPOCH; // evita NPE no sort
    }

    @Transactional(readOnly = true)
    public DashboardOverviewDto loadOverviewForTeacher(Long teacherUserId) {
        long activeSubs = subscriptionRepository.countActiveByTeacherUserId(teacherUserId);
        long students = subscriptionRepository.countDistinctActiveStudents(teacherUserId);
        long scheduled = lessonRepository.countFutureLessonsByTeacher(teacherUserId, OffsetDateTime.now());
        return new DashboardOverviewDto(students, scheduled, activeSubs);
    }

    @Transactional(readOnly = true)
    public List<RecentActivityDto> loadRecentActivities(Long teacherUserId, int limit) {
        var page = PageRequest.of(0, Math.min(Math.max(limit, 1), 100));

        var enrollments = subscriptionRepository.findRecentEnrollments(teacherUserId, page)
                .stream().map(v -> new RecentActivityDto(v.getStudentEmail(), "Nova matrícula", toInstant(v.getHappenedAt())))
                .toList();

        var payments = paymentRepository.findRecentPayments(teacherUserId, page)
                .stream().map(v -> new RecentActivityDto(v.getStudentEmail(), "Pagamento realizado", toInstant(v.getHappenedAt())))
                .toList();

        var lessons = lessonRepository.findRecentChanges(teacherUserId, page)
                .stream().map(v -> new RecentActivityDto(v.getStudentEmail(), v.getEventLabel(), toInstant(v.getHappenedAt())))
                .toList();

        var merged = new ArrayList<RecentActivityDto>();
        merged.addAll(enrollments);
        merged.addAll(payments);
        merged.addAll(lessons);

        merged.sort(Comparator.comparing(RecentActivityDto::getHappenedAt).reversed());
        return merged.size() > page.getPageSize() ? merged.subList(0, page.getPageSize()) : merged;
    }
}
