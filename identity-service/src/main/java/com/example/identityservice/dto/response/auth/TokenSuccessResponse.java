package com.example.identityservice.dto.response.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenSuccessResponse {
    private String accessToken;
    private String refreshToken;
}