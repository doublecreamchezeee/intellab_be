package com.example.courseservice.dto.response.learningLesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonProgressResponse {
    UUID lesson_id;
    UUID course_id;
    Integer lesson_order;
    String lesson_name;
    String description;
    String content;
    UUID problem_id;
    UUID exercise_id;
    String status;
    Instant last_accessed_date;
}