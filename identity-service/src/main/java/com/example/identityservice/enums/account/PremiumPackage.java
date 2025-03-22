package com.example.identityservice.enums.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumPackage {

    PREMIUM_PLAN("premium plan", 1000000L, "VND"),
    PROBLEM_PLAN("problem plan", 600000L, "VND"),
    COURSE_PLAN("course plan", 600000L, "VND")
    ;
    String code;
    Long price;
    String unitPrice;

    static String VND = "VND";
}
