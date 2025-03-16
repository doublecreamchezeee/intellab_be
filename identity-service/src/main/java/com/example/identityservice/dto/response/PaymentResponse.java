package com.example.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PaymentResponse {
    String id;
    String uid;
    String currency;
    String amount;
    String type;
    String description;
    Date timestamp;
}
