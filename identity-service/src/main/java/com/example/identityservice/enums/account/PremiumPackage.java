package com.example.identityservice.enums.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumPackage {
    DIAMOND_PACKAGE("DIAMOND", 1000000L),
    PLATINUM_PACKAGE("PLATINUM", 500000L),
    GOLD_PACKAGE("GOLD", 200000L),
    SILVER_PACKAGE("SILVER", 100000L),
    BRONZE_PACKAGE("BRONZE", 50000L),
    ;
    String code;
    Long price;
}
