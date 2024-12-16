package com.example.courseservice.dto.request.Assignment;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentDetailRequest {
    String answer;
    String unitScore;
    UUID questionId;
}
