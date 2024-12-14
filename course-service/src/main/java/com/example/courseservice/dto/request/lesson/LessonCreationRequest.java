package com.example.courseservice.dto.request.lesson;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonCreationRequest {
    @Min(value = 0, message = "LESSON_ORDER_INVALID")
    int lessonOrder;
    String lessonName;
    String description;
    String content;
    UUID courseId;
}
