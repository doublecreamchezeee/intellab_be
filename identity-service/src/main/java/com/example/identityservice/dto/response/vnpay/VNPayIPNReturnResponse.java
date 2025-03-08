package com.example.identityservice.dto.response.vnpay;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayIPNReturnResponse {
    String RspCode;
    String Message;
}
