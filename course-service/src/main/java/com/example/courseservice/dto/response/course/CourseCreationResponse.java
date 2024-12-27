package com.example.courseservice.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCreationResponse {
    UUID courseId;
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    String courseLogo;
    UUID userUid;
    Integer reviewCount;
    Double averageRating;
    int lessonCount;
}
