package com.example.courseservice.dto.response.Comment;


import com.example.courseservice.model.Comment;
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
    UUID userId;
    String userName;
    String avatarUrl;

    List<CommentResponse> comments;
}
