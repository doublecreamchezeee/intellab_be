package com.example.identityservice.dto.request.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReEnrollCoursesUsingSubscriptionPlanRequest {
    UUID userUuid;
    String subscriptionPlan;
}
