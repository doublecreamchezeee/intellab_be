package com.example.identityservice.constant.response.vnpay;

import java.util.HashMap;
import java.util.Map;

public class VNPayRefundResponseCode {
    public static final Map<String, String> RESPONSE_CODE_MAP = new HashMap<>();

    static {
        RESPONSE_CODE_MAP.put("00", "Request successful");
        RESPONSE_CODE_MAP.put("02", "Invalid connection identifier (check TmnCode)");
        RESPONSE_CODE_MAP.put("03", "Data sent is not in the correct format");
        RESPONSE_CODE_MAP.put("91", "Refund transaction not found");
        RESPONSE_CODE_MAP.put("94", "Refund request already sent. VNPAY is processing this request");
        RESPONSE_CODE_MAP.put("95", "Transaction failed at VNPAY. VNPAY refuses to process the request");
        RESPONSE_CODE_MAP.put("97", "Invalid checksum");
        RESPONSE_CODE_MAP.put("99", "Other errors (remaining errors, not listed in the error code list)");
    }

    public static String getDescription(String code) {
        return RESPONSE_CODE_MAP.get(code);
    }
}
