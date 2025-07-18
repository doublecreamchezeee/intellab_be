package com.example.identityservice.dto.response.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
public class UserInfoResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private Boolean isEmailVerified;
    private String phoneNumber;
    private String photoUrl;
    private String role;
    private boolean isDisabled;
    private Date lastSignIn;
    private int courseCount;
    private Boolean isPublic;
    public Date getLastSignIn() {
        if (lastSignIn == null || lastSignIn.toString().contains("1970")) {
            return null;
        }
        return  this.lastSignIn;
    }
}
