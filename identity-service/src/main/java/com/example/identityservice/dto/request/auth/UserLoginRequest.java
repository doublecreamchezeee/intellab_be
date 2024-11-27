package com.example.identityservice.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email must be of valid format")
    private String email;

    @NotBlank(message = "Password must not be empty")
    private String password;
}
