package com.example.courseservice.dto.request.Question;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionCreationRequest {
    UUID lessonId;
    String questionContent;

    String correctAnswer;
    Character questionType;
}

