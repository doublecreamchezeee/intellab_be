package com.example.problemservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressLevelResponse {
    private int totalProblems;
    private DifficultyStatistics easy;
    private DifficultyStatistics medium;
    private DifficultyStatistics hard;
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DifficultyStatistics {
        private int solved;
        private int max;
    }
}
