package com.example.identityservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Payment {
    String paymentId;
    String uid;
    String currency;
    String type;
    Date createdAt;
}
