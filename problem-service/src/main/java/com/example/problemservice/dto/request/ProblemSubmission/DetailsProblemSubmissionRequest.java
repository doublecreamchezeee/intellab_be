package com.example.problemservice.dto.request.ProblemSubmission;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsProblemSubmissionRequest {
    String code;
    Integer languageId;
    UUID problemId;
    String userUid;
}
