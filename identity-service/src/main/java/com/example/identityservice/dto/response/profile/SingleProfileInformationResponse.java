package com.example.identityservice.dto.response.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

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
    Boolean isEmailVerified;
    String role;
    boolean isDisabled;
    Date lastSignIn;
    boolean isPublic;

    public Date getLastSignIn() {
        if (lastSignIn == null || lastSignIn.toString().contains("1970")) {
            return null;
        }
        return  this.lastSignIn;
    }
}
