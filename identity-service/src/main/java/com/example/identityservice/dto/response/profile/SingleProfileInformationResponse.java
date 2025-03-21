package com.example.identityservice.dto.response.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleProfileInformationResponse {
    String userId;
    String displayName;
    String email;
    String phoneNumber;
    String photoUrl;

    String firstName;
    String lastName;
    boolean emailVerified;
    String role;
    boolean isDisabled;
    Date lastSignIn;

    public Date getLastSignIn() {
        if (lastSignIn == null || lastSignIn.toString().contains("1970")) {
            return null;
        }
        return  this.lastSignIn;
    }
}
