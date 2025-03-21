package com.example.identityservice.dto.response.auth;

import com.google.cloud.firestore.DocumentReference;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class PremiumSubscription {
    Date startDate;
    Date endDate;
    String status;
    String planType;
}
