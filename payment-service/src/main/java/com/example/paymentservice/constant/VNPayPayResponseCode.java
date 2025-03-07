package com.example.paymentservice.constant;

import java.util.HashMap;
import java.util.Map;

public class VNPayPayResponseCode {
    public static final Map<String, String> RESPONSE_CODE_MAP = new HashMap<>();

    static {
        RESPONSE_CODE_MAP.put("00", "Transaction successful");
        RESPONSE_CODE_MAP.put("07", "Money deducted successfully. Transaction suspected (related to fraud, unusual transaction).");
        RESPONSE_CODE_MAP.put("09", "Transaction failed: Customer's card/account has not registered for InternetBanking service at the bank.");
        RESPONSE_CODE_MAP.put("10", "Transaction failed: Customer's card/account information verification failed more than 3 times.");
        RESPONSE_CODE_MAP.put("11", "Transaction failed: Payment timeout. Please try again.");
        RESPONSE_CODE_MAP.put("12", "Transaction failed: Customer's card/account is locked.");
        RESPONSE_CODE_MAP.put("13", "Transaction failed: Incorrect transaction authentication password (OTP). Please try again.");
        RESPONSE_CODE_MAP.put("24", "Transaction failed: Customer canceled the transaction.");
        RESPONSE_CODE_MAP.put("51", "Transaction failed: Insufficient balance in customer's account.");
        RESPONSE_CODE_MAP.put("65", "Transaction failed: Customer's account has exceeded the daily transaction limit.");
        RESPONSE_CODE_MAP.put("75", "Payment bank is under maintenance.");
        RESPONSE_CODE_MAP.put("79", "Transaction failed: Customer entered incorrect payment password too many times. Please try again.");
        RESPONSE_CODE_MAP.put("99", "Other errors (remaining errors, not listed in the error code list).");
    }

    public static String getDescription(String code) {
        return RESPONSE_CODE_MAP.get(code);
    }
}
