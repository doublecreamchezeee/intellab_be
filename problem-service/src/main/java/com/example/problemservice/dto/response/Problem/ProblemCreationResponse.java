package com.example.problemservice.dto.response.Problem;

import com.example.problemservice.core.ProblemStructure;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
}
