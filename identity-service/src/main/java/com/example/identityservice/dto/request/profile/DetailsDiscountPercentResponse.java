package com.example.identityservice.dto.request.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsDiscountPercentResponse {
    float discountPercent;
    float discountValue;
}
