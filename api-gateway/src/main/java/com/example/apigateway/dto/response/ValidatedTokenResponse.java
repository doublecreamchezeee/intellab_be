package com.example.apigateway.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ValidatedTokenResponse {
    boolean isValidated;
    String message;
    String userId;
    String email;
    String role;
    String premium;
    Boolean isEmailVerified;
}
