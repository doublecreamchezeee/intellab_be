package com.example.problemservice.dto.request.solution;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SolutionCreationRequest {
    String content;
    String problemId;
    String authorId;
}
