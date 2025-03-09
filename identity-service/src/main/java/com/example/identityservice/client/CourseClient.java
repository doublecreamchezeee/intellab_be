package com.example.identityservice.client;

import com.example.identityservice.dto.response.LeaderboardCourseResponse;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CourseClient {
    @GetExchange("/statistics/leaderboard")
    Mono<List<LeaderboardCourseResponse>> getLeaderboard();
}
