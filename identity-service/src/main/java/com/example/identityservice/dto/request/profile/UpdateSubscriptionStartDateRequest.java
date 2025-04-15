package com.example.identityservice.dto.request.profile;


import com.example.identityservice.enums.account.PremiumPackageDiscountPercentByTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSubscriptionStartDateRequest {
    PremiumPackageDiscountPercentByTime discountPercentByTime;
}
