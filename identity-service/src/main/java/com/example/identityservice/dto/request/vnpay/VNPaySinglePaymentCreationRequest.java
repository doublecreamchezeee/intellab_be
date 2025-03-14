package com.example.identityservice.dto.request.vnpay;

import com.example.identityservice.enums.vnpay.VNPayBankCode;
import com.example.identityservice.enums.vnpay.VNPayCurrencyCode;
import com.example.identityservice.enums.vnpay.VNPayLocale;
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
    VNPayLocale language;
    UUID courseId;
}
