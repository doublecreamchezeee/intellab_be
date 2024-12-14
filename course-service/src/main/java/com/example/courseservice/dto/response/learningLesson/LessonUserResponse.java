package com.example.courseservice.dto.response.learningLesson;

import com.example.courseservice.model.Lesson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LessonUserResponse {
    UUID learningId;
    String status;
    Instant lastAccessedDate;
    UUID userId;
    UUID lessonId;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    Lesson lesson;
}
