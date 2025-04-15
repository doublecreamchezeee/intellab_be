package com.example.courseservice.dto.response.course;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseAndFirstLessonResponse {
    UUID courseId;
    String courseName;
    String courseDescription;
    String level;
    float price;
    String unitPrice;
    Integer reviewCount;
    Double averageRating;
    int lessonCount;

    UUID lessonId;
    String lessonContent;
    String lessonDescription;
    int lessonOrder;
    String lessonName;
    UUID exerciseId;
    UUID problemId;
}
