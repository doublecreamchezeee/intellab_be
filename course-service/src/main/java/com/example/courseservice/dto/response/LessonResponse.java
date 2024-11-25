package com.example.courseservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonResponse {
    String id;
    int lessonOrder;
    String name;
    String description;
    String content;
    String courseId;
}
