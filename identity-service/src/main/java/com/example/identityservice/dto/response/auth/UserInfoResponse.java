package com.example.identityservice.dto.response.auth;

import com.example.identityservice.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

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
    private boolean emailVerified;
    private String phoneNumber;
    private String photoUrl;
    private String role;
    private boolean isDisabled;
    private Date lastSignIn;

    public Date getLastSignIn() {
        if (lastSignIn == null || lastSignIn.toString().contains("1970")) {
            return null;
        }
        return  this.lastSignIn;
    }
}
