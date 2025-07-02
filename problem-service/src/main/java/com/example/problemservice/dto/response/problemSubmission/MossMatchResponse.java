package com.example.problemservice.dto.response.problemSubmission;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MossMatchResponse {
    String submissionId1;
    String userId1;
    String submissionId2;
    String userId2;
    int percent;
}
