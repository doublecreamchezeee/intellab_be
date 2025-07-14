package com.example.problemservice.dto.response.Problem;

import com.example.problemservice.core.ProblemStructure;
import com.example.problemservice.dto.response.problemSubmission.ProblemSubmissionResponse;
import com.example.problemservice.dto.response.solution.SolutionCreationResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemCreationResponse {
    String problemId;
    String problemName;
    String description;
    String problemLevel;
    Integer score;
    Float acceptanceRate;
    Boolean isAvailable;
    Boolean isPublished;
    ProblemStructure problemStructure;
    Boolean hasSolution;
    Boolean isCompletedCreation;
    Integer currentCreationStep;
    String currentCreationStepDescription;
    List<CategoryResponse> categories;
    ProblemSubmissionStat problemSubmissionStat;
    SolutionCreationResponse solution;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static public class ProblemSubmissionStat {
        Integer total;
        Integer pass;
        Integer fail;
    }

    Date createdAt;
    Boolean hasCustomChecker;
    String additionalCheckerFields;
}
