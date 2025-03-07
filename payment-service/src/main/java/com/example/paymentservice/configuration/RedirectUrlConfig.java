package com.example.paymentservice.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RedirectUrlConfig {
    @Value("${FE_URL}")
    String feUrl;


    String redirectUrl = feUrl + "/payment-result?paymentId=";

    /*String successUrl = "http://localhost:3000/payment-result";
    String failedUrl = "http://localhost:3000/payment-result";
    String cancelUrl = "http://localhost:3000/payment-result";
    String ipnUrl  = "http://localhost:3000/payment-result";*/

}
