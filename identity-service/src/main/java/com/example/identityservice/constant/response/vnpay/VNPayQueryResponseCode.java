package com.example.identityservice.constant.response.vnpay;

import java.util.HashMap;
import java.util.Map;

public class VNPayQueryResponseCode {
    public static final Map<String, String> RESPONSE_CODE_MAP = new HashMap<>();

    static {
        RESPONSE_CODE_MAP.put("00", "Request successful");
        RESPONSE_CODE_MAP.put("02", "Invalid connection identifier (check TmnCode)");
        RESPONSE_CODE_MAP.put("03", "Data sent is not in the correct format");
        RESPONSE_CODE_MAP.put("91", "Transaction not found");
        RESPONSE_CODE_MAP.put("94", "Duplicate request within the API's time limit");
        RESPONSE_CODE_MAP.put("97", "Invalid checksum");
        RESPONSE_CODE_MAP.put("99", "Other errors (remaining errors, not listed in the error code list)");
    }

    public static String getDescription(String code) {
        return RESPONSE_CODE_MAP.get(code);
    }
}
