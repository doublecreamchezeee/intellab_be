package com.example.courseservice.dto.request.learningLesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningLessonUpdateRequest {
    String status;
    //UUID learningId;
}
