package com.example.paymentservice.constant;

import java.util.HashMap;
import java.util.Map;

public class VNPayTransactionStatus {
    public static final Map<String, String> TRANSACTION_STATUS_MAP = new HashMap<>();

    static {
        TRANSACTION_STATUS_MAP.put("00", "Transaction successful");
        TRANSACTION_STATUS_MAP.put("01", "Transaction not completed");
        TRANSACTION_STATUS_MAP.put("02", "Transaction error");
        TRANSACTION_STATUS_MAP.put("04", "Reversed transaction (Customer was debited at the bank but the transaction was not successful at VNPAY)");
        TRANSACTION_STATUS_MAP.put("05", "VNPAY is processing this transaction (Refund transaction)");
        TRANSACTION_STATUS_MAP.put("06", "VNPAY has sent a refund request to the bank (Refund transaction)");
        TRANSACTION_STATUS_MAP.put("07", "Transaction suspected of fraud");
        TRANSACTION_STATUS_MAP.put("09", "Refund transaction denied");
    }

    public static String getDescription(String code) {
        return TRANSACTION_STATUS_MAP.get(code);
    }
}
