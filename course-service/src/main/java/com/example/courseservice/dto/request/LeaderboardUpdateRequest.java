package com.example.courseservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardUpdateRequest {
    private String userId;
    private String type; // "problem" or "course"
    private Long newScore;
    private ProblemStat problemStat;
    private CourseStat courseStat;

    @Data
    @Builder
    public static class ProblemStat  {
        Integer easy;
        Integer medium;
        Integer hard;
        Integer total;
    }

    @Data
    @Builder
    public static class CourseStat {
        Integer beginner;
        Integer intermediate;
        Integer advanced;
        Integer total;
    }
}
