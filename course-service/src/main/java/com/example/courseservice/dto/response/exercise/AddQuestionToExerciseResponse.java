package com.example.courseservice.dto.response.exercise;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddQuestionToExerciseResponse {
    UUID exerciseId;
    UUID questionId;
}
