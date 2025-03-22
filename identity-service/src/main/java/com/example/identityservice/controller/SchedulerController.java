package com.example.identityservice.controller;

import com.example.identityservice.configuration.SchedulerConfig;
import com.example.identityservice.dto.ApiResponse;
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
    SchedulerConfig schedulerConfig;

    @Operation(
            summary = "Update cron job time to check end date of premium package",
            description = """
                    Format: * * * * * *
                    """
    )
    @PostMapping("update-cron")
    public ApiResponse<String> updateCronExpression(@RequestBody String cronExpression) {
        schedulerConfig.setCustomCronExpression(cronExpression);
        return ApiResponse.<String>builder()
                .message("Cron expressions updated successfully")
                .result(cronExpression)
                .build();
    }
}
