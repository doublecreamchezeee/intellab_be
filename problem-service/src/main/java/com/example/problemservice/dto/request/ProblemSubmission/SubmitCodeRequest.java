package com.example.problemservice.dto.request.ProblemSubmission;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitCodeRequest {
    int submitOrder;
    String code;
    String programmingLanguage;
    String problemId;
    String userId;
}
