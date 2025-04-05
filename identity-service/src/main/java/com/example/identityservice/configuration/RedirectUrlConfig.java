package com.example.identityservice.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RedirectUrlConfig {
    @Value("${FE_URL}")
    String feUrl;

    @Value("${HOST_NAME}")
    String beHostName;

    @Value("${IDENTITY_PORT}")
    String bePort;

    String redirectUrl;// = feUrl + "/payment-result?paymentId=";

    String updateAccessTokenUrl;// = feUrl + "/update-access-token";
    @PostConstruct
    public void init(){
        this.redirectUrl = feUrl + "/payment-result?paymentId=";
        this.updateAccessTokenUrl = feUrl + "/profile/update-access-token";
    }
    /*String successUrl = "http://localhost:3000/payment-result";
    String failedUrl = "http://localhost:3000/payment-result";
    String cancelUrl = "http://localhost:3000/payment-result";
    String ipnUrl  = "http://localhost:3000/payment-result";*/

}
