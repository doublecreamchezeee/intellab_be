package com.example.problemservice.dto.response.Problem;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemRowResponse {
    UUID problemId;
    String problemName;
    String level;
    Float acceptanceRate;
    boolean isDone;
    Integer hintCount;
    List<CategoryRessponse> categories;
}
