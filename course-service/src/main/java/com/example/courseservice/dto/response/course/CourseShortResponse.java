package com.example.courseservice.dto.response.course;

import com.example.courseservice.dto.response.category.CategoryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseShortResponse {
    UUID id;
    String name;
    Double rating;
    Integer reviewCount;
    List<CategoryResponse> categories;
}
