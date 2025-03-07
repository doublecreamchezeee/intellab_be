package com.example.problemservice.dto.response.problemSubmission;

import com.example.problemservice.dto.response.Problem.CategoryResponse;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemCategory;
import com.example.problemservice.model.TestCaseOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsProblemSubmissionResponse {
    UUID submissionId;
    Integer submissionOrder;
    String code;
    String programmingLanguage;
    Integer scoreAchieved;
    ProblemResponse problem;
    UUID userUid;
    Boolean isSolved;
    List<TestCaseOutput> testCasesOutput;

    Float usedMemory;
    Float runtime;

    @Data
    @Builder
    public static class ProblemResponse {
        String problemName;
        UUID problemId;
        List<CategoryResponse> categories;
    }
}
