package com.example.courseservice.dto.response.lesson;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsLessonResponse {
    UUID lessonId;
    String content;
    String description;
    int lessonOrder;
    String lessonName;
    UUID courseId;
    UUID exerciseId;
    UUID learningId;
    UUID nextLessonId;
    String nextLessonName;
    Boolean isDoneTheory;
    Boolean isDonePractice;
}
