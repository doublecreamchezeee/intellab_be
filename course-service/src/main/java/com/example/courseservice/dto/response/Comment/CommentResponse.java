package com.example.courseservice.dto.response.Comment;


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
public class CommentResponse {
    UUID commentId;
    String content;
    Long numberOfLikes;
    Instant created;
    Instant lastModified;
    UUID userId;
    String userUid;
    String userName;
    String avatarUrl;
    UUID repliedCommentId;
    UUID parentCommentId;
    Boolean isModified;
    Boolean isUpvoted;
    Boolean isOwner;

    Page<CommentResponse> comments;
}
