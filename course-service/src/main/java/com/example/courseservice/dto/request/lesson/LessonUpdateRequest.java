package com.example.courseservice.dto.request.lesson;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonUpdateRequest {
    @Min(value = 0, message = "LESSON_ORDER_INVALID")
    int lessonOrder;
    String lesson_name;
    String description;
    String content;
    UUID course_id;
}
