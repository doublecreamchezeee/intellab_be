package com.example.courseservice.dto.response.Question;


import com.example.courseservice.dto.response.Option.OptionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {
    UUID questionId;
    String questionContent;
    String status;
    String correctAnswer;
    Character questionType;
    //Instant createdAt;
    //Instant updatedAt;

    List<OptionResponse> options;
}

//UUID question_id;
//String questionContent;
//// enable, disable, pending
//String status;
//String correct_answer;
//// S: single-choice; M: multiple-choice
//Character question_type;
//Instant created_at;
//Instant updated_at;
