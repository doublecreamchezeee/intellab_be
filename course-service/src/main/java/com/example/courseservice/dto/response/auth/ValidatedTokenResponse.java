package com.example.courseservice.dto.response.auth;

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
    String name;
}
