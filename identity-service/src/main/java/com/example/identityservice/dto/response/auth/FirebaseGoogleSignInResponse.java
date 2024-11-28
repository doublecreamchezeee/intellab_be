package com.example.identityservice.dto.response.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FirebaseGoogleSignInResponse {
    private String uid;
    private String email;
}
