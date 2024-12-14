package com.example.courseservice.dto.response.userCourses;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCoursesResponse {
    String courseName;
    String description;
    String level;
    float price;
    String unitPrice;
    String courseLogo;
    float progressPercent;
    String status;
    Instant lastAccessedDate;
}
