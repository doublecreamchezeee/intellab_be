package com.example.courseservice.dto.response.Assignment;

import com.example.courseservice.dto.response.Question.QuestionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentDetailResponse {

    Integer order;
    String answer;
    Integer unitScore;
    QuestionResponse question;

}
