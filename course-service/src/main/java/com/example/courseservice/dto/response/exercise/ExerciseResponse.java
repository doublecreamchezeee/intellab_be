package com.example.courseservice.dto.response.exercise;


import com.example.courseservice.dto.response.Question.QuestionCreationResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseResponse {
    UUID exerciseId = null;
    Boolean isQuizVisible = false;
    Integer questionsPerExercise = 0;
    Integer passingQuestions = 0;
    List<QuestionCreationResponse> questionList = new ArrayList<>();
}
