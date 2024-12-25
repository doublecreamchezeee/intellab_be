package com.example.courseservice.dto.response.rerview;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsReviewResponse {
    UUID reviewId;
    int rating;
    String comment;
    UUID userUid;
    UUID courseId;
}
