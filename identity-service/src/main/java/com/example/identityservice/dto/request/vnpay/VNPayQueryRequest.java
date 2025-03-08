package com.example.identityservice.dto.request.vnpay;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayQueryRequest {
    String vnp_RequestId;
    String vnp_Version;
    String vnp_Command;
    String vnp_TmnCode;
    String vnp_TxnRef;
    String vnp_OrderInfo;
    String vnp_TransactionDate;
    String vnp_CreateDate;
    String vnp_IpAddr;
    String vnp_SecureHash;
}
