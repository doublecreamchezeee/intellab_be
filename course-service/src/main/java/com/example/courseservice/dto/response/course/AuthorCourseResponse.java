package com.example.courseservice.dto.response.course;


import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.model.Section;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorCourseResponse {
    UUID courseId;
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    UUID userId;
    Double averageRating;

    Integer reviewCount;
    int lessonCount;

    List<CategoryResponse> categories;
    List<Section> sections;
    List<LessonResponse> lessons;
}
