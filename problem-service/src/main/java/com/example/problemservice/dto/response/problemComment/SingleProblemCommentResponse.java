package com.example.problemservice.dto.response.problemComment;

import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleProblemCommentResponse {
    String commentId;
    String content;
    Long numberOfLikes;

    String problemId;
    String userUid;
    String userUuid;

    @Nullable
    String parentCommentId;

    @Nullable
    UUID replyToCommentId;

    Instant createdAt;
    Instant lastModifiedAt;

    @Nullable
    String username;

    @Nullable
    String userAvatar;

    @Nullable
    String userEmail;

    Boolean isModified;
}
