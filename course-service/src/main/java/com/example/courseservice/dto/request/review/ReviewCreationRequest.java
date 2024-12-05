package com.example.courseservice.dto.request.review;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationRequest {
    int rating;
    String comment;
    UUID userUid;
}
