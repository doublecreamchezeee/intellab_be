package com.example.courseservice.controller;

import com.example.courseservice.dto.response.LeaderboardResponse;
import com.example.courseservice.service.StatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Statistics")
public class StatisticsController {
    StatisticsService statisticsService;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {
        List<LeaderboardResponse> response = statisticsService.getCourseLeaderboard();
        return ResponseEntity.ok(response);
    }
}
