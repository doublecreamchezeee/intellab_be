package com.example.identityservice.mapper;

import java.util.List;

import com.example.identityservice.dto.response.admin.AdminUserResponse;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.mapper.impl.UserMapperImpl;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.UserRecord;

public interface UserMapper {
    public static UserMapper INSTANCE = new UserMapperImpl();
    UserInfoResponse fromRecordToResponse(UserRecord userRecord);
    List<UserInfoResponse> fromRecordsToResponses(List<UserRecord> userRecords);
    public AdminUserResponse fromRecordToResponse(ExportedUserRecord record);

}
