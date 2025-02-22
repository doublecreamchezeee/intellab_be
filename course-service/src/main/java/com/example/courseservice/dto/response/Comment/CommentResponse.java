package com.example.courseservice.dto.response.Comment;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
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
    String userName;
    String avatarUrl;
    UUID repliedCommentId;
    UUID parentCommentId;
    Boolean isModified;
    Boolean isUpVoted;

    List<CommentResponse> comments;
}
