package com.example.identityservice.mapper;

import com.example.identityservice.dto.request.AccountCreationRequest;
import com.example.identityservice.dto.request.AccountUpdateRequest;
import com.example.identityservice.dto.response.AccountResponse;
import com.example.identityservice.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    Account toAccount(AccountCreationRequest request);

    AccountResponse toAccountResponse(Account account);
    @Mapping(target = "roles", ignore = true)
    void updateAccount(@MappingTarget Account account, AccountUpdateRequest request);
}
