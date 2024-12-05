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
    UUID Lesson_Id;
    int lessonOrder;
    String lesson_name;
    String description;
    String content;
    UUID course_id;
}
