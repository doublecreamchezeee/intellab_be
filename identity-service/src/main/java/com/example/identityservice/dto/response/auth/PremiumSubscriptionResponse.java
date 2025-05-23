package com.example.identityservice.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PremiumSubscriptionResponse {
    String status;
    String planType;
    Instant startDate;
    Instant endDate;
    Integer durationInDays;
    String userUid;
    UUID userUuid;
    String durationEnums;
}
