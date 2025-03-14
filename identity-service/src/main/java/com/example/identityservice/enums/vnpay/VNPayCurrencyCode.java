package com.example.identityservice.enums.vnpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public enum VNPayCurrencyCode {
    VND("VND"),
    ;

    String code;
}
