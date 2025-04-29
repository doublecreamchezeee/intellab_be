package com.example.courseservice.dto.response.course;

import com.example.courseservice.dto.response.category.CategoryResponse;
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
public class CourseCreationResponse {
    UUID courseId;
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    UUID userUid;
    Integer reviewCount;
    Double averageRating;
    int lessonCount;
    Boolean isAvailable;
    Integer currentCreationStep;
    String currentCreationStepDescription;
    Boolean isCompletedCreation;
    String courseImage;

    List<CategoryResponse> categories;
    List<Section> sections;
}
