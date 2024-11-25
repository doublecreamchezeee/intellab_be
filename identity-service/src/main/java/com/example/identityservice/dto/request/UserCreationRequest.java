package com.example.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreationRequest {

    @NotBlank(message = "EmailId must not be empty")
    @Email(message = "EmailId must be of valid format")
    private String emailId;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 6, message = "Password length must be 6 characters long")
    private String password;

}
