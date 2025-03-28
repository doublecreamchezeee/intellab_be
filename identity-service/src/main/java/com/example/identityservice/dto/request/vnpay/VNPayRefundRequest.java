package com.example.identityservice.dto.request.vnpay;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayRefundRequest {
    String vnp_RequestId;
    String vnp_Version;
    String vnp_Command;
    String vnp_TmnCode;
    String vnp_TransactionType;
    String vnp_TxnRef;
    String vnp_Amount;
    String vnp_OrderInfo;
    String vnp_TransactionNo;
    String vnp_TransactionDate;
    String vnp_CreateBy;
    String vnp_CreateDate;
    String vnp_IpAddr;
    String vnp_SecureHash;
}
