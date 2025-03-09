package com.example.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardCourseResponse {
    long point;
    String userId;
    CourseStatResponse courseStat;
    @Data
    @Builder
    public static class CourseStatResponse {
        int beginner;
        int intermediate;
        int advanced;
        int total;
    }
}
