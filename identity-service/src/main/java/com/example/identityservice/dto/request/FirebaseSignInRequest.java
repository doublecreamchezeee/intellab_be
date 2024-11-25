package com.example.identityservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirebaseSignInRequest {
    private String email;
    private String password;
    private boolean returnSecureToken;
}
