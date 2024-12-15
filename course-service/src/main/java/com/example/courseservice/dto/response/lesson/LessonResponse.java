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
    int lessonOrder;
    String lessonName;
    String description;
    String content;
    UUID courseId;
}
