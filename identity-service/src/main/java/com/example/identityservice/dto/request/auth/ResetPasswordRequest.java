package com.example.identityservice.dto.request.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    String token;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 6, message = "Password length must be at least 6 characters long")
    String newPassword;
}
