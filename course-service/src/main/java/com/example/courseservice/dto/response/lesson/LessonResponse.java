package com.example.courseservice.dto.response.lesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonResponse {
    UUID lessonId;
    String content;
    String description;
    int lessonOrder;
    String lessonName;
    UUID courseId;
    UUID quizId;
    UUID problemId;
}
