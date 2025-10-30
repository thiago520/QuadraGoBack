package com.quadrago.backend.dashboard;

import com.quadrago.backend.dashboard.dto.DashboardOverviewDto;
import com.quadrago.backend.dashboard.dto.RecentActivityDto;
import com.quadrago.backend.dashboard.support.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewDto> overview() {
        Long userId = CurrentUser.id();
        return ResponseEntity.ok(service.loadOverviewForTeacher(userId));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<RecentActivityDto>> activities(
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = CurrentUser.id();
        return ResponseEntity.ok(service.loadRecentActivities(userId, limit));
    }
}
