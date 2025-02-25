package com.example.problemservice.controller;

import com.example.problemservice.dto.response.ProgressLanguageResponse;
import com.example.problemservice.dto.response.ProgressLevelResponse;
import com.example.problemservice.service.StatisticsService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Statistics")
public class StatisticsController {
    StatisticsService statisticsService;

    @GetMapping("/progress/level")
    public ResponseEntity<ProgressLevelResponse> getProgressLevel(@RequestHeader("X-UserId") String userId) {
        userId = userId.split(",")[0];
        ProgressLevelResponse response = statisticsService.getProgressLevel(ParseUUID.normalizeUID(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/language")
    public ResponseEntity<ProgressLanguageResponse> getProgressLanguage(@RequestHeader("X-UserId") String userId) {
        userId = userId.split(",")[0];
        ProgressLanguageResponse response = statisticsService.getProgressLanguage(ParseUUID.normalizeUID(userId));
        return ResponseEntity.ok(response);
    }
}
