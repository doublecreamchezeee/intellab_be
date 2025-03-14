package com.example.identityservice.dto.response.userCourse;


import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCoursesResponse {

    EnrollCourse enrollId;
    ResponseCourse course;
    Float progressPercent;
    String status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private class EnrollCourse {
        String courseId;
        String userUid;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private class ResponseCourse {
        UUID courseId;
        String courseName;
        String description;
        String level;
        Float price;
        String unitPrice;
        Double averageRating;
        Integer reviewCount;
    }
}
