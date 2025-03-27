package com.example.courseservice.dto.request.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisenrollCoursesEnrolledUsingSubscriptionPlanRequest {
    List<UUID> listUserUuid;
}
