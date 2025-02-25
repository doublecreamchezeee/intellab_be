package com.example.problemservice.dto.request.problemComment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemCommentUpdateRequest {
    String content;
}
