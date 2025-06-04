package com.example.courseservice.dto.request.exercise;

import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModifyQuizRequest {
    UUID lessonId;
    Boolean isQuizVisible = false;
    Integer questionsPerExercise = null;
    Integer passingQuestions = null;
    List<QuestionUpdateRequest> questions = new ArrayList<>();
}
