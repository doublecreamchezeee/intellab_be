package com.example.paymentservice.dto.response.vnpay;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayQueryResponse {
    String vnp_ResponseId;
    String vnp_Command;
    String vnp_TmnCode;
    String vnp_TxnRef;
    String vnp_Amount;
    String vnp_OrderInfo;
    String vnp_ResponseCode;
    String vnp_Message;
    String vnp_BankCode;
    String vnp_PayDate;
    String vnp_TransactionNo;
    String vnp_TransactionType;
    String vnp_TransactionStatus;
    String vnp_PromotionCode;
    String vnp_PromotionAmount;
    String vnp_SecureHash;
    String vnp_TransactionStatusDescription;
    String vnp_ResponseCodeDescription;
}
