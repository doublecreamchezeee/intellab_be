package com.example.courseservice.dto.response.rerview;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationResponse {
    String id;
    int rating;
    String comment;
    String userUid;
    String courseId;
}
