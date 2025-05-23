package com.example.identityservice.enums.account;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PremiumPackageDiscountPercentByTime {
    FIRST_QUARTER(0.7f, 0.25f),
    SECOND_QUARTER(0.5f, 0.5f),
    THIRD_QUARTER(0.2f, 0.75f),
    FOURTH_QUARTER(0.0f, 1.0f)
    ;
    float discountPercent;
    float durationPercent;

}
