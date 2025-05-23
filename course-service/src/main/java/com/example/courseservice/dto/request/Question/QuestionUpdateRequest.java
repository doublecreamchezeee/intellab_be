package com.example.courseservice.dto.request.Question;


import com.example.courseservice.dto.request.Option.OptionRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionUpdateRequest {
    UUID questionId;
    String questionContent;
    String correctAnswer;
    Character questionType;
    List<OptionRequest> optionRequests;
}
