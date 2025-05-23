package com.example.courseservice.dto.request.lesson;

import jakarta.annotation.Nullable;
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
    UUID courseId;
    UUID clonedLessonId;
}
