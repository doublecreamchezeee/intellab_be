package com.example.identityservice.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {
    @Email(message = "Email must be of valid format")
    private String email;

    @Size(min = 6, message = "Password length must be at least 6 characters long")
    private String password;

    @Size(max = 50, message = "Display name length must not exceed 50 characters")
    private String displayName;

    @Pattern(
            regexp = "^\\+[1-9][0-9]{1,14}$",
            message = "Phone number must be E.164 compliant (e.g., +84123456789)"
    )
    private String phoneNumber;

    @Size(max = 255, message = "Photo URL length must not exceed 255 characters")
    private String photoUrl;
}
