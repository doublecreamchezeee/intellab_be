package com.example.courseservice.dto.request.comment;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentModifyRequest {
    UUID commentId;
    String content;
}
