package com.example.identityservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PaymentRequest {
    String uid;
    String currency;
    String amount;
    String type;
    String description;
}
