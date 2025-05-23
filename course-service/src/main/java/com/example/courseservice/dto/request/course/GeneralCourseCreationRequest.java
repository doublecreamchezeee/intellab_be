package com.example.courseservice.dto.request.course;

import com.example.courseservice.enums.course.CourseLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeneralCourseCreationRequest {
    @NotNull
    String courseName;
    @NotNull
    String description;
    @NotNull
    CourseLevel level;
    @NotNull
    List<Integer> categoryIds;
}
