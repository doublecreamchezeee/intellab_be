package com.example.courseservice.dto.response.course;

import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.model.Section;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCourseCreationResponse {
    UUID courseId;
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    UUID teacherUuid;
    Integer reviewCount;
    Double averageRating;
    int lessonCount;
    Boolean isAvailable;
    Integer currentCreationStep;
    String currentCreationStepDescription;
    Boolean isCompletedCreation;
    String courseImage;

    @Nullable
    Integer numberOfEnrolledStudents;

    List<CategoryResponse> categories;
    List<Section> sections;
}
