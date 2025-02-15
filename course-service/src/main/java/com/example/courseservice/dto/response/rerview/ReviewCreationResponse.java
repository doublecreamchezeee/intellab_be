package com.example.courseservice.dto.response.rerview;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationResponse {
    UUID reviewId;
    int rating;
    String comment;
    UUID userUuid;
    String userUid;
    UUID courseId;
    Instant createAt;
    Instant lastModifiedAt;
}
