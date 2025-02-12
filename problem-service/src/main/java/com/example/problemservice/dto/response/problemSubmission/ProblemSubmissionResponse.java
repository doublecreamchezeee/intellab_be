package com.example.problemservice.dto.response.problemSubmission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemSubmissionResponse {
    String submissionId;
    String status;
    Date submitDate;
    String programmingLanguage;
    double runtime;
    double memory;
}
