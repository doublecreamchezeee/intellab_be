package com.example.problemservice.dto.response.problemSubmission;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
    UUID problemId;
    UUID userUid;
}
