package com.example.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardResponse {
    long point;
    String displayName;
    String firstName;
    String lastName;
    String photoUrl;
    CourseStatResponse courseStat;
    ProblemStatResponse problemStat;

    @AllArgsConstructor
    @Data
    @Builder
    public static class CourseStatResponse {
        int beginner;
        int intermediate;
        int advanced;
        int total;
    }

    @AllArgsConstructor
    @Data
    @Builder
    public static class ProblemStatResponse {
        int easy;
        int medium;
        int hard;
        int total;
    }
}
