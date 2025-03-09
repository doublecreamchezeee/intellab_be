package com.example.problemservice.controller;

import com.example.problemservice.dto.response.LeaderboardResponse;
import com.example.problemservice.dto.response.ProgressLanguageResponse;
import com.example.problemservice.dto.response.ProgressLevelResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.service.StatisticsService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Statistics")
public class StatisticsController {
    StatisticsService statisticsService;

    @GetMapping("/progress/level")
    public ResponseEntity<ProgressLevelResponse> getProgressLevel(
            @RequestHeader(name = "X-UserId", required = false) String userUid,
            @RequestParam (required = false) String UserUid) {

        if (userUid == null && UserUid == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (UserUid != null) {
            ProgressLevelResponse response = statisticsService.getProgressLevel(ParseUUID.normalizeUID(UserUid));
            return ResponseEntity.ok(response);
        }

        userUid = userUid.split(",")[0];
        ProgressLevelResponse response = statisticsService.getProgressLevel(ParseUUID.normalizeUID(userUid));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/language")
    public ResponseEntity<ProgressLanguageResponse> getProgressLanguage(
            @RequestHeader(name = "X-UserId", required = false) String userUid,
            @RequestParam (required = false) String UserUid) {

        if (userUid == null && UserUid == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (UserUid != null) {
            ProgressLanguageResponse response = statisticsService.getProgressLanguage(ParseUUID.normalizeUID(UserUid));
            return ResponseEntity.ok(response);
        }

        userUid = userUid.split(",")[0];
        ProgressLanguageResponse response = statisticsService.getProgressLanguage(ParseUUID.normalizeUID(userUid));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(){
        List<LeaderboardResponse> response = statisticsService.getProblemLeaderboard();
        return ResponseEntity.ok(response);
    }
}
