package com.example.problemservice.dto.request.problemComment;

import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemCommentCreationRequest {
    String content;
    String problemId;

    @Nullable
    String parentCommentId;

    @Nullable
    String replyToCommentId;
}
