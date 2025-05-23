package com.example.courseservice.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseResponse {
    UUID courseId;
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    UUID userUid;
    int numberOfReviews;
    float averageRating;
    String courseImage;
}
