package com.example.identityservice.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.example.identityservice.service.AdminDashboardService;

import java.util.UUID;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin Dashboard")
public class AdminLessonController {
    AdminDashboardService dashboardService;
    final String defaultRole = "myRole";

    private Boolean isAdmin(String role) {
        return role.contains("admin");
    }

    @PostMapping
    public ApiResponse<List<DashboardMetricResponse>> createLesson(
            @RequestHeader(value = "X-UserRole") String userRole) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<List<DashboardMetricResponse>>builder()
                .result(dashboardService.getSystemOverview())
                .build();
    }
}
