package com.example.problemservice.model.course;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lesson {
    UUID lessonId;
    String lessonName;
    String description;
    String content;
    int lessonOrder;
    Course course;
}
