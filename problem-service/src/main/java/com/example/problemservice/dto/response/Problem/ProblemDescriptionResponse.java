package com.example.problemservice.dto.response.Problem;

import com.example.problemservice.model.course.Category;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemDescriptionResponse {
    UUID problemId;
    String description;
    String level;
    List<Category> categories;
}
