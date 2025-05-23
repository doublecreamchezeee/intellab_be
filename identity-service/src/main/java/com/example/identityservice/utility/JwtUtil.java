package com.example.identityservice.utility;


import com.example.identityservice.dto.response.auth.ResetPasswordSessionToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Encoders;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class JwtUtil {

    @Value("${APP_CUSTOM_SECRET_KEY}")
    private String appCustomSecretKey;

    //@Bean
    public String generateJwtByUserUid(String userUid) {
        long expirationTimeInMillis = 15 * 60 * 1000; // 15 minutes
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + expirationTimeInMillis);

        //log.info("expirationTime: {}", expirationTime);

        return Jwts.builder()
                .setSubject(userUid) // Add user UID as the subject
                .setIssuedAt(now) // Set the issue time
                .setExpiration(expirationTime) // Set the expiration time
                .signWith(SignatureAlgorithm.HS256, getSigningKey()) // Sign with the secret key
                .compact();
    }

    public Claims decodeJwt(String jwt) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            log.error("Error decoding JWT: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public ResetPasswordSessionToken getUserUidAndExpiration(String jwt) {
        try {
            Claims claims = decodeJwt(jwt);
            String userUid = claims.getSubject(); // Extract user UID
            Date expirationDate = claims.getExpiration(); // Extract expirationDate date

            /*Map<String, Object> result = new HashMap<>();
            result.put("userUid", userUid);
            result.put("expirationDate", expirationDate);*/

            return ResetPasswordSessionToken.builder()
                    .userUid(userUid)
                    .expirationDate(expirationDate)
                    .build();
        } catch (Exception e) {
            log.error("Error extracting user UID and expiration: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token or session token expired", e);
        }
    }

    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appCustomSecretKey);
        //String keyBytes =  Encoders.BASE64.encode(appCustomSecretKey.getBytes());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static Key getKeyFromString(String keyString) {
        try {
            // Convert the string to a byte array
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);

            // Create a SecretKeySpec using the byte array and algorithm
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception e) {
            throw new RuntimeException("Error generating key: " + e.getMessage(), e);
        }
    }
}
