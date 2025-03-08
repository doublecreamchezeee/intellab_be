package com.example.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardResponse {
    long point;
    String displayName;
    String firstName;
    String lastName;
    CourseStatResponse courseStat;
    ProblemStatResponse problemStat;

    @Data
    @Builder
    public static class CourseStatResponse {
        int beginner;
        int intermediate;
        int advanced;
        int total;
    }

    @Data
    @Builder
    public static class ProblemStatResponse {
        int easy;
        int medium;
        int hard;
        int total;
    }
}
