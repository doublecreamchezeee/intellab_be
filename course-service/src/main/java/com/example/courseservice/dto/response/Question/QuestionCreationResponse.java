package com.example.courseservice.dto.response.Question;


import com.example.courseservice.dto.response.Option.OptionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionCreationResponse {
    UUID questionId;
    String questionContent;
    String status;
    String correctAnswer;
    Character questionType;
    List<OptionResponse> options;
}
