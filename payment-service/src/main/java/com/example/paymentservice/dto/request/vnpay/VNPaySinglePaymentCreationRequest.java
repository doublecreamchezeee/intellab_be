package com.example.paymentservice.dto.request.vnpay;

import com.example.paymentservice.constant.VNPayBankCode;
import com.example.paymentservice.constant.VNPayCurrencyCode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPaySinglePaymentCreationRequest {
    VNPayBankCode VNPayBankCode;
    VNPayCurrencyCode VNPayCurrencyCode;
    UUID courseId;
}
