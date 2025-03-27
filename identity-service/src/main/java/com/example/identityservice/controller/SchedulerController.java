package com.example.identityservice.controller;

import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.configuration.SchedulerConfig;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.scheduler.VNPayPaymentPremiumPackageScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulerController {
    private SchedulerConfig schedulerConfig;
    private VNPayPaymentPremiumPackageScheduler vnPayPaymentPremiumPackageScheduler;

    @Operation(
            summary = "Update cron job time to check end date of subscription plan",
            description = """
                    Format: * * * * * *
                    """
    )
    @PublicEndpoint
    @PostMapping("update-cron")
    public ApiResponse<String> updateCronExpression(@RequestBody String cronExpression) {
        schedulerConfig.setCustomCronExpression(cronExpression);
        return ApiResponse.<String>builder()
                .message("Cron expressions updated successfully")
                .result(cronExpression)
                .build();
    }

    @Operation(
            summary = "Get current cron job time to check end date of subscription plan",
            description = """
                    Format: * * * * * *
                    """
    )
    @PublicEndpoint
    @GetMapping("get-cron")
    public ApiResponse<String> getCronExpression() {
        return ApiResponse.<String>builder()
                .message("Cron expressions retrieved successfully")
                .result(schedulerConfig.getCustomCronExpression())
                .build();
    }

    @Operation(
            summary = "Check end date of subscription plan immediately"
    )
    @PublicEndpoint
    @PostMapping("check-end-date")
    public ApiResponse<Boolean> checkEndDate() {
        vnPayPaymentPremiumPackageScheduler.checkPremiumPackageEndDate();
        return ApiResponse.<Boolean>builder()
                .message("Check end date of subscription plan successfully")
                .result(true)
                .build();
    }
}
