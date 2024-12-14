package com.example.courseservice.dto.response.learningLesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningLessonResponse {
    UUID learningId;
    String status;
    Instant lastAccessedDate;
    UUID userId;
    UUID lessonId;
}
