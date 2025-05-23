package com.example.identityservice.dto.response.auth;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenSuccessResponse {
    private String accessToken;
    private String refreshToken;
    private String userRole;
}