package com.example.identityservice.controller.admin;

import com.example.identityservice.dto.response.DashboardMetricResponse;
import com.example.identityservice.dto.response.DashboardTableResponse;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.response.chart.ChartResponse;
import com.example.identityservice.service.AdminDashboardService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin Dashboard")
public class AdminDashboardController {
    AdminDashboardService dashboardService;

    private Boolean isAdmin(String role) {
        return role.contains("admin");
    }

    @GetMapping("/overview")
    public ApiResponse<List<DashboardMetricResponse>> getOverview(
            @RequestHeader(value = "X-UserRole") String userRole) throws FirebaseAuthException {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<List<DashboardMetricResponse>>builder()
                .result(dashboardService.getSystemOverview())
                .build();
    }

    @GetMapping("/subscription-growth")
    public ApiResponse<ChartResponse> getSubscriptionGrowth(
            @RequestHeader(value = "X-UserRole") String userRole,
            @RequestParam String type,
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<ChartResponse>builder()
                .result(dashboardService.getSubscriptionGrowth(type, startDate, endDate))
                .build();
    }

    @GetMapping("/revenue")
    public ApiResponse<ChartResponse> getRevenue(
            @RequestHeader(value = "X-UserRole") String userRole,
            @RequestParam String type,
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<ChartResponse>builder()
                .result(dashboardService.getRevenue(type, startDate, endDate))
                .build();
    }

    @GetMapping("/user-growth")
    public ApiResponse<ChartResponse> getUserGrowth(
            @RequestHeader(value = "X-UserRole") String userRole,
            @RequestParam String type,
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws FirebaseAuthException {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<ChartResponse>builder()
                .result(dashboardService.getUserGrowth(type, startDate, endDate))
                .build();
    }

    @GetMapping("/course-completion-rate")
    public ApiResponse<ChartResponse> getCourseCompletionRate(
            @RequestHeader(value = "X-UserRole") String userRole,
            @RequestParam String type,
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws FirebaseAuthException {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<ChartResponse>builder()
                .result(dashboardService.getCourseCompletionRate(type, startDate, endDate))
                .build();
    }

    @GetMapping("/transactions")
    public ApiResponse<Page<DashboardTableResponse>> getTransactions(
            @RequestHeader(value = "X-UserRole") String userRole,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Page<DashboardTableResponse> transactions = dashboardService.getFilteredTransactions(keyword,
                type, status, search, sortBy, order, pageable
        );

        return ApiResponse.<Page<DashboardTableResponse>>builder()
                .result(transactions)
                .build();
    }


    @GetMapping("/top-purchased")
    public ApiResponse<Page<DashboardTableResponse>> getTopPurchased(
            @RequestHeader(value = "X-UserRole") String userRole,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "amount") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            Pageable pageable
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Page<DashboardTableResponse> topPurchased = dashboardService.getTopPurchased(
                type, search, sortBy, order, pageable
        );

        return ApiResponse.<Page<DashboardTableResponse>>builder()
                .result(topPurchased)
                .build();
    }


}
