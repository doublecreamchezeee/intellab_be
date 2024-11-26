package com.example.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank(message = "EmailId must not be empty")
    @Email(message = "EmailId must be of valid format")
    private String emailId;

    @NotBlank(message = "Password must not be empty")
    private String password;
}