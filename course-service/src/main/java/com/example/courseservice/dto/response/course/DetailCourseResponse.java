package com.example.courseservice.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailCourseResponse {
    String id;
    String name;
    String description;
    String level;
    boolean isUserEnrolled;
}
