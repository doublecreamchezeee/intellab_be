package com.example.identityservice.enums.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumPackageStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    EXPIRED("Expired"),
    ;
    String code;
}
