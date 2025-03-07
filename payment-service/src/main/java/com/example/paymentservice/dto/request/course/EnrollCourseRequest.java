package com.example.paymentservice.dto.request.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class EnrollCourseRequest {
    UUID courseId;
    String userUid;
}
