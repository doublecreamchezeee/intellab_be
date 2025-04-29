package com.example.courseservice.dto.response.exercise;


import com.example.courseservice.dto.response.Question.QuestionCreationResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseResponse {
    UUID exerciseId;
    Integer questionsPerExercise;
    Integer passingQuestions;
    List<QuestionCreationResponse> questionList;
}
