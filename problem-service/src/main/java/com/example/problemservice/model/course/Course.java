package com.example.problemservice.model.course;

import lombok.*;
import lombok.experimental.FieldDefaults;


import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Course {
    UUID courseId;
    String courseName;
    String description;
    String level;
    Float price;
    String unitPrice;
    String courseLogo;
}