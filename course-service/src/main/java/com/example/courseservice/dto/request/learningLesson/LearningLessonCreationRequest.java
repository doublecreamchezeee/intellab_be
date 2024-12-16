package com.example.courseservice.dto.request.learningLesson;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningLessonCreationRequest {
    @NotBlank
    UUID lessonId;
    String userId;

}
