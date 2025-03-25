package com.example.identityservice.dto;

import com.example.identityservice.model.CourseStat;
import com.example.identityservice.model.ProblemStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardUpdateRequest {
    private String userId;
    private String type; // "problem" or "course"
    private Long additionalScore;
    private ProblemStat problemStat;
    private CourseStat courseStat;
}
