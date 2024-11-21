package com.example.identityservice.service;

import com.example.identityservice.dto.request.AccountCreationRequest;
import com.example.identityservice.dto.request.AccountUpdateRequest;
import com.example.identityservice.dto.response.AccountResponse;
import com.example.identityservice.enums.Role;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import com.example.identityservice.mapper.AccountMapper;
import com.example.identityservice.mapper.ProfileMapper;
import com.example.identityservice.mapper.AccountMapper;
import com.example.identityservice.model.Account;
import com.example.identityservice.repository.RoleRepository;
import com.example.identityservice.repository.AccountRepository;
import com.example.identityservice.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountService {
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    ProfileClient profileClient;
    ProfileMapper profileMapper;

    public AccountResponse createAccount(AccountCreationRequest request) {
        if (accountRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);
        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        var rolesToAdd = roleRepository.findAllById(roles);
        account.setRoles(new HashSet<>(rolesToAdd));
        account = accountRepository.save(account);
//        var profileRequest = profileMapper.toProfileCreationRequest(request);
//        var profileResponse = profileClient.createProfile(profileRequest);
        return accountMapper.toAccountResponse(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountResponse> getAccounts(){
        return accountRepository.findAll().stream()
                .map(accountMapper::toAccountResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AccountResponse getAccount(String id){
        return accountMapper.toAccountResponse(accountRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
    }



    @PostAuthorize("returnObject.username == authentication.name")
    public AccountResponse updateAccount(String accountID, AccountUpdateRequest request){
        Account account = accountRepository.findById(accountID).orElseThrow(() -> new RuntimeException("User not found"));
        accountMapper.updateAccount(account, request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        account.setRoles(new HashSet<>(roles));
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAccount(String accountId) {
        accountRepository.deleteById(accountId);
    }


    public AccountResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Account account = accountRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return accountMapper.toAccountResponse(account);
    }
}
