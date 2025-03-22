package com.example.identityservice.enums.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumDuration {
    MONTHLY_PACKAGE("MONTHLY", 30),
    YEARLY_PACKAGE("YEARLY", 365),
    ;
    String code;
    Integer duration; // in days
}
