package com.example.identityservice.dto.response.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenSuccessResponse {
    private String id_token;
    private String refresh_token;
    private String expires_in;
}
