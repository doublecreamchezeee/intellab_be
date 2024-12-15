package com.example.courseservice.dto.response.exercise;


import com.example.courseservice.model.Question;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseResponse {
    UUID exerciseId;
    String exerciseName;
    String exerciseDescription;
}
