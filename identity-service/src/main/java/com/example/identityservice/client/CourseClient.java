package com.example.identityservice.client;

import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.response.LeaderboardCourseResponse;
import com.example.identityservice.dto.response.course.CompleteCourseResponse;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CourseClient {
    @GetExchange("/statistics/leaderboard")
    Mono<List<LeaderboardCourseResponse>> getLeaderboard();

    @GetExchange("/courses/courseList/me")
    Mono<ApiResponse<List<CompleteCourseResponse>>> getCourseByUserId();

}
