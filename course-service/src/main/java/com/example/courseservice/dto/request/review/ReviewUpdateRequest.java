package com.example.courseservice.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewUpdateRequest {
    @Min(value = 1)
    @Max(value = 5)
    int rating;
    String comment;

}
