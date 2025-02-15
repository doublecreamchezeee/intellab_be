package com.example.courseservice.dto.response.course;

import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
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
    float averageRating;
    int reviewCount;
    boolean isUserEnrolled;
    UUID latestLessonId;
    Float progressPercent;
    String certificateUrl;
    String certificateId;
}
