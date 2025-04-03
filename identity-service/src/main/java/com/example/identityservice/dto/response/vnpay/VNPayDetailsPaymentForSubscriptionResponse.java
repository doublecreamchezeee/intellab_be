package com.example.identityservice.dto.response.vnpay;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayDetailsPaymentForSubscriptionResponse extends VNPayDetailsPaymentForCourseResponse {
    UUID paymentPremiumPackageId;
    Instant startDate;
    String packageType;
    Instant endDate;
    String status;
    Integer duration;
}
