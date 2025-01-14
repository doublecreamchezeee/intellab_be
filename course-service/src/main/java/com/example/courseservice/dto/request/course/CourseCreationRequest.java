package com.example.courseservice.dto.request.course;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCreationRequest {
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    String userUid;
}
