package com.example.identityservice.dto.response.vnpay;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayDetailsPaymentForCourseResponse {
    UUID paymentId;
    String userUid;
    UUID userUuid;
    String transactionStatus;
    String responseCode;
    Float totalPaymentAmount;
    Float paidAmount;
    String currency;
    String bankCode;
    String transactionReference;
    Instant createdAt;
    Instant receivedAt;
    String bankTransactionNo;
    String transactionNo;
    String transactionStatusDescription;
    String responseCodeDescription;
    String orderDescription;
    String paymentFor;
}
