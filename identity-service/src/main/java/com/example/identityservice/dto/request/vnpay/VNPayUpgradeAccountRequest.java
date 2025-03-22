package com.example.identityservice.dto.request.vnpay;

import com.example.identityservice.enums.account.PremiumDuration;
import com.example.identityservice.enums.account.PremiumPackage;
import com.example.identityservice.enums.vnpay.VNPayBankCode;
import com.example.identityservice.enums.vnpay.VNPayCurrencyCode;
import com.example.identityservice.enums.vnpay.VNPayLocale;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayUpgradeAccountRequest {
    VNPayBankCode VNPayBankCode;
    VNPayCurrencyCode VNPayCurrencyCode;
    VNPayLocale language;
    PremiumPackage premiumPackage;
    PremiumDuration premiumDuration;
}
