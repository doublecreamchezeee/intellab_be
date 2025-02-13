package com.example.courseservice.dto.response.rerview;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseReviewsStatisticsResponse {
    Integer totalReviews;
    Double averageRating;
    Integer fiveStar;
    Integer fourStar;
    Integer threeStar;
    Integer twoStar;
    Integer oneStar;
    UUID courseId;
}
