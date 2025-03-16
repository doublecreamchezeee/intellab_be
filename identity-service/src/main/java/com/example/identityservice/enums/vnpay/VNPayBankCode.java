package com.example.identityservice.enums.vnpay;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum VNPayBankCode {
    VNBANK("VNBANK"),
    VNPAYQR("VNPAYQR"),
    INT_CARD("INT_CARD"),
    ;

    String code;
}
