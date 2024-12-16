package com.example.courseservice.dto.request.Question;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionUpdateRequest {

    String questionContent;
    // available, unavailable
    String status;
    String correctAnswer;
    Character questionType;
}
