package com.example.courseservice.dto.response.learningLesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonProgressResponse {
    UUID lessonId;
    UUID courseId;
    Integer lessonOrder;
    String lessonName;
    String description;
    String content;
    UUID problemId;
    UUID exerciseId;
    String status;
    Instant lastAccessedDate;
    UUID learningId;
    Boolean isDoneTheory;
    Boolean isDonePractice;
    /*UUID lesson_id;
    UUID course_id;
    Integer lesson_order;
    String lesson_name;
    String description;
    String content;
    UUID problem_id;
    UUID exercise_id;
    String status;
    Instant last_accessed_date;*/
}