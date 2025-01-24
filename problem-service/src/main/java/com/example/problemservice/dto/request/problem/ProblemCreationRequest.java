package com.example.problemservice.dto.request.problem;

import com.example.problemservice.core.ProblemStructure;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemCreationRequest {
    String problemName;
    String description;
    String problemLevel;
    Integer score;
    Float acceptanceRate;
    Boolean isAvailable;
    Boolean isPublished;
    ProblemStructure problemStructure;
}
