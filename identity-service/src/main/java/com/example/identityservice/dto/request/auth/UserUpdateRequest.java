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
    @Size(max = 50, message = "Display name length must not exceed 50 characters")
    private String displayName;

    @Size(max = 50, message = "First name length must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "First name length must not exceed 50 characters")
    private String lastName;

    @Size(max = 255, message = "Photo URL length must not exceed 255 characters")
    private String photoUrl;

    private String password;
}
