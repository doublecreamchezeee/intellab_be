package com.example.identityservice.dto.response.vnpay;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayPaymentUrlResponse {
    String paymentUrl;
    String transactionReference;
    Date currentDate;
}
