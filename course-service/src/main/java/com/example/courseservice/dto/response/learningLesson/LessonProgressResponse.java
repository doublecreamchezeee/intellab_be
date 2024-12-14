package com.example.courseservice.dto.response.learningLesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonProgressResponse {
    UUID lessonId;
    UUID courseId;
    Integer lessonOrder;
    String lessonName;
    String description;
    String content;
    UUID problemId;
    UUID exerciseId;
    String status;
    ZonedDateTime lastAccessedDate;
}