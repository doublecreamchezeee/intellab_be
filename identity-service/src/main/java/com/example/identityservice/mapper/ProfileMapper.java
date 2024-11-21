package com.example.identityservice.mapper;

import com.example.identityservice.dto.request.AccountCreationRequest;
import com.example.identityservice.dto.request.ProfileCreationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel =  "spring")
public interface ProfileMapper {
//    @Mapping(source = "firstName", target = "firstName")
//    @Mapping(source = "lastName", target = "lastName")
//    @Mapping(source = "dob", target = "dob")
//    @Mapping(source = "city", target = "city")
//    @Mapping(source = "username", target = "username")
    ProfileCreationRequest toProfileCreationRequest(AccountCreationRequest request);
}