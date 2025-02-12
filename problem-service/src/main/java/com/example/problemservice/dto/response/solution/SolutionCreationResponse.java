package com.example.problemservice.dto.response.solution;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SolutionCreationResponse {
    String content;
    String problemId;
    String authorId;
}
