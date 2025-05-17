package com.example.identityservice.mapper.impl;

import java.util.Date;
import java.util.List;


import com.example.identityservice.dto.response.admin.AdminUserResponse;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.mapper.UserMapper;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.UserRecord;


public class UserMapperImpl implements UserMapper {
    public static final UserMapperImpl INSTANCE = new UserMapperImpl();

    @Override
    public UserInfoResponse fromRecordToResponse(UserRecord userRecord) {
        String role = userRecord.getCustomClaims().get("role") != null ? userRecord.getCustomClaims().get("role").toString() : "USER";
        return UserInfoResponse.builder()
                .userId(userRecord.getUid())
                .email(userRecord.getEmail())
                .displayName(userRecord.getDisplayName())
                .phoneNumber(userRecord.getPhoneNumber())
                .photoUrl(userRecord.getPhotoUrl())
                .isDisabled(userRecord.isDisabled())
                .lastSignIn(new Date(userRecord.getUserMetadata().getLastSignInTimestamp()))
                .isEmailVerified(userRecord.isEmailVerified())
                .build();
    }

    @Override
    public List<UserInfoResponse> fromRecordsToResponses(List<UserRecord> userRecords) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromRecordsToResponses'");
    }

    @Override
    public AdminUserResponse fromRecordToResponse(ExportedUserRecord record) {
        return AdminUserResponse.builder()
                .creationTimestamp(new Date(record.getUserMetadata().getCreationTimestamp()))
                .lastSignInTimestamp(new Date(record.getUserMetadata().getLastSignInTimestamp()))
                .userUid(record.getUid())
                .email(record.getEmail())
                .firstName(record.getDisplayName())
                .lastName(record.getDisplayName())
                .displayName(record.getDisplayName())
                .isEmailVerified(record.isEmailVerified())
                .role(null)
                .premiumType(null)
                .packageDuration(null)
                .build();
               /* .role(record.getCustomClaims().get("role") != null ? record.getCustomClaims().get("role").toString() : "USER")
                .premiumType(record.getCustomClaims().get("premiumType") != null ? record.getCustomClaims().get("premiumType").toString() : "FREE")
                .packageDuration(record.getCustomClaims().get("packageDuration") != null ? record.getCustomClaims().get("packageDuration").toString() : "FREE")*/

    }


}
