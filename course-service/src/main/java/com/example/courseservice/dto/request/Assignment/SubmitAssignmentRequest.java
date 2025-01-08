package com.example.courseservice.dto.request.Assignment;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAssignmentRequest {
    Float score;
    UUID exerciseId;
    UUID learningId;

    List<AssignmentDetailRequest> assignmentDetailRequests;
}
