package com.example.identityservice.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FirebaseGoogleSignUpResponse {
    String uid;
    String email;
    Boolean isSignUpSuccessful;
    String message;
}
