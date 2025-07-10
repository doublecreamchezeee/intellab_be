package com.example.problemservice.dto.response.Problem;

import com.example.problemservice.model.*;
import com.example.problemservice.model.course.Category;
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
    Boolean ViewedSolution;
    List<Category> categories;
    List<Hint> hints;
    Boolean hasSolution;
    Boolean isSolved;
    Boolean hasCustomChecker;
    String additionalCheckerFields;
}
