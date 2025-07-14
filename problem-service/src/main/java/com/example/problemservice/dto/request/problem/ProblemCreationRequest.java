package com.example.problemservice.dto.request.problem;

import com.example.problemservice.core.ProblemStructure;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemCreationRequest {
    String problemId;
    String problemName;
    String description;
    String problemLevel;
    Integer score;
    Boolean isPublished;
    ProblemStructure problemStructure;
    List<Integer> categories;

}
