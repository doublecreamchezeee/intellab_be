package com.example.courseservice.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCreationResponse {
    UUID course_id;
    String course_name;
    String description;
    String level;
}
