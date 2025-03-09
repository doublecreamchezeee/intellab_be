package com.example.courseservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardResponse {
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
