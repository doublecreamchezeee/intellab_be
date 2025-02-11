package com.example.courseservice.dto.response.exercise;


import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Question;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseDetailResponse {
    UUID exerciseId;
    String exerciseName;
    String exerciseDescription;

    List<UUID> questionIds;

    public ExerciseDetailResponse(Exercise exercise, List<UUID> questionIds)
    {
        this.exerciseId = exercise.getExerciseId();
        this.exerciseName = exercise.getExerciseName();
        this.exerciseDescription = exercise.getDescription();
        this.questionIds = questionIds;
    }
}
