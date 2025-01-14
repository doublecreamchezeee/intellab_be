package com.example.courseservice.dto.response.Assignment;


import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentResponse {
    UUID assignmentId;
    UUID learningLessonId;
    Float score;
    Integer submitOrder;
    Instant submitDate;

}
