package com.example.courseservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class LeaderboardUpdateRequest {
    private String userId;
    private String type; // "problem" or "course"
    private Long additionalScore;
    private ProblemStat problemStat;
    private CourseStat courseStat;

    public LeaderboardUpdateRequest()
    {
        problemStat = new ProblemStat(0,0,0,0);
        courseStat = new CourseStat(0,0,0,0);
    }

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
