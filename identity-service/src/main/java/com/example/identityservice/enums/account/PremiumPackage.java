package com.example.identityservice.enums.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumPackage {

    PREMIUM_PLAN("PREMIUM_PLAN", 499000L, "VND", "Premium Plan"),
    ALGORITHM_PLAN("ALGORITHM_PLAN", 299000L, "VND", "Algorithm Plan"),
    COURSE_PLAN("COURSE_PLAN", 299000L, "VND", "Course Plan"),
    ;
    String code;
    Long price;
    String unitPrice;
    String name;

    static String VND = "VND";
}
