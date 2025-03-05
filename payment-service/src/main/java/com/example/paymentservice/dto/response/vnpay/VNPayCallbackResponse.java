package com.example.paymentservice.dto.response.vnpay;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayCallbackResponse {
    String vnp_TmnCode;
    String vnp_Amount;
    String vnp_BankCode;
    String vnp_BankTranNo;
    String vnp_CardType;
    String vnp_PayDate;
    String vnp_OrderInfo;
    String vnp_TransactionNo;
    String vnp_ResponseCode;
    String vnp_TransactionStatus;
    String vnp_TxnRef;
    String vnp_SecureHash;
}
