package com.example.problemservice.dto.response.Problem;

import com.example.problemservice.model.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsProblemResponse {
    String problemId;
    String problemName;
    String description;
    String problemLevel;
    Integer score;
    Float acceptanceRate;
    Boolean isAvailable;
    Boolean isPublished;
    List<TestCase> testCases;
    Solution solution;
    List<ProblemCategory> categories;
    List<Hint> hints;
    Boolean hasSolution;
}
