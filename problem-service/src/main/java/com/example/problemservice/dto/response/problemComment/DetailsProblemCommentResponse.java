package com.example.problemservice.dto.response.problemComment;

import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsProblemCommentResponse {
    UUID commentId;
    String content;
    Long numberOfLikes;

    UUID problemId;
    String userUid;
    UUID userUuid;

    @Nullable
    UUID parentCommentId;

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

    Boolean isUpVoted;

    Page<DetailsProblemCommentResponse> childrenComments;
}
