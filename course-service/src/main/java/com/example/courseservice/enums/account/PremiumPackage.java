package com.example.courseservice.enums.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumPackage {

    PREMIUM_PLAN("PREMIUM_PLAN", 499000L, "VND"),
    ALGORITHM_PLAN("ALGORITHM_PLAN", 299000L, "VND"),
    COURSE_PLAN("COURSE_PLAN", 299000L, "VND")
    ;
    String code;
    Long price;
    String unitPrice;

    static String VND = "VND";
}
