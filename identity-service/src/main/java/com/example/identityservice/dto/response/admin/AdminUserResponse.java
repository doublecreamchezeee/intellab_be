package com.example.identityservice.dto.response.admin;

import java.util.Date;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserResponse {
    Date creationTimestamp;
    Date lastSignInTimestamp;
    String userUid;
    String email;
    String firstName;
    String lastName;
    String displayName;
    Boolean isEmailVerified;
    String role;
    String premiumType; //PREMIUM_PLAN, free
    String packageDuration; // YEARLY_PACKAGE, MONTHLY_PACKAGE
}
