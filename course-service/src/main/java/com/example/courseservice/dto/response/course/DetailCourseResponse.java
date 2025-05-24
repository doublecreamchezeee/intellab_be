package com.example.courseservice.dto.response.course;

import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import jakarta.annotation.Nullable;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailCourseResponse {
    UUID courseId;
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    UUID userUid;
    int lessonCount;
    Double averageRating;
    Integer reviewCount;
    boolean isUserEnrolled;
    UUID latestLessonId;
    Float progressPercent;
    String certificateUrl;
    String certificateId;
    String courseImage;
    Integer templateCode;
    Instant createdAt;

    @Nullable
    Integer numberOfEnrolledStudents;

    @Nullable
    String aiSummaryContent;

    @Nullable
    String templateLink;

    @Nullable
    List<CategoryResponse> categories;
}
