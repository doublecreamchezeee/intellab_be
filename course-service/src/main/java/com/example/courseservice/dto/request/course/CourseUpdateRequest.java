package com.example.courseservice.dto.request.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseUpdateRequest {
    String name;
    String description;
    String level;
}
