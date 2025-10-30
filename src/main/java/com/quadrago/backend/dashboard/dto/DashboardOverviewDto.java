package com.quadrago.backend.dashboard.dto;

public record DashboardOverviewDto(
        long students,
        long scheduledLessons,
        long activeSubscriptions
) {
}
