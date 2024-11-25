package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.dto.request.UserCreationRequest;
import com.example.identityservice.dto.request.UserLoginRequest;
import com.example.identityservice.dto.response.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.TokenSuccessResponse;
import com.example.identityservice.exception.AccountAlreadyExistsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthClient firebaseAuthClient;

    @SneakyThrows
    public void create(@NonNull final UserCreationRequest userCreationRequest) {
        final var request = new UserRecord.CreateRequest();
        request.setEmail(userCreationRequest.getEmailId());
        request.setPassword(userCreationRequest.getPassword());
        request.setEmailVerified(Boolean.TRUE);

        try {
            firebaseAuth.createUser(request);
        } catch (final FirebaseAuthException exception) {
            if (exception.getMessage().contains("EMAIL_EXISTS")) {
                throw new AccountAlreadyExistsException("Account with provided email-id already exists");
            }
            throw exception;
        }
    }

    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) {
        return firebaseAuthClient.login(userLoginRequest);
    }

    public RefreshTokenSuccessResponse refreshAccessToken(@NonNull final String refreshToken) {
        return firebaseAuthClient.refreshAccessToken(refreshToken);
    }
}
