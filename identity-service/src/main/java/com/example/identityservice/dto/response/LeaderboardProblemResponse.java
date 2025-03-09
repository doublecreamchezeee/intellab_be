package com.example.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardProblemResponse {
    long point;
    String userId;
    ProblemStatResponse problemStat;
    @Data
    @Builder
    public static class ProblemStatResponse {
        int easy;
        int medium;
        int hard;
        int total;
    }
}
