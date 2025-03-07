package com.example.paymentservice.utils;

import java.security.MessageDigest;
import java.util.UUID;

public class ParseUUID {

    public static UUID normalizeUID(String uid) {
        try {
            // Sử dụng SHA-256 để băm UID Firebase
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(uid.getBytes());

            // Tạo UUID từ 16 byte đầu tiên của hash
            long mostSignificantBits = 0;
            long leastSignificantBits = 0;

            for (int i = 0; i < 8; i++) {
                mostSignificantBits |= ((long) hash[i] & 0xff) << (8 * (7 - i));
            }
            for (int i = 8; i < 16; i++) {
                leastSignificantBits |= ((long) hash[i] & 0xff) << (8 * (15 - i));
            }

            return new UUID(mostSignificantBits, leastSignificantBits);
        } catch (Exception e) {
            throw new RuntimeException("Error while converting UID to UUID", e);
        }
    }
}
