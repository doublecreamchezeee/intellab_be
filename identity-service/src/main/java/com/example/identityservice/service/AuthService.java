package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.response.auth.FirebaseGoogleSignInResponse;
import com.example.identityservice.dto.response.auth.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.auth.TokenSuccessResponse;
import com.example.identityservice.exception.AccountAlreadyExistsException;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthClient firebaseAuthClient;

    @SneakyThrows
    public void create(@NonNull final UserCreationRequest userCreationRequest) {
        log.info("Creating user with email: {}", userCreationRequest.getEmail());
        final var request = new UserRecord.CreateRequest()
                .setEmail(userCreationRequest.getEmail())
                .setPassword(userCreationRequest.getPassword())
                .setEmailVerified(Boolean.TRUE)
                .setDisplayName(userCreationRequest.getDisplayName())
                .setPhoneNumber(userCreationRequest.getPhoneNumber());
                //.setPhotoUrl(userCreationRequest.getPhotoUrl());

        if (!Strings.isNullOrEmpty(userCreationRequest.getPhotoUrl())) {
            request.setPhotoUrl(userCreationRequest.getPhotoUrl());
        }

        try {
            firebaseAuth.createUser(request);
            log.info("User successfully created: {}", userCreationRequest.getEmail());
        } catch (final FirebaseAuthException exception) {
            if (exception.getMessage().contains("EMAIL_EXISTS")) {
                throw new AccountAlreadyExistsException("Account with provided email already exists");
            }
            if (exception.getMessage().contains("PHONE_NUMBER_EXISTS")) {
                throw new AccountAlreadyExistsException("Account with provided phone number already exists");
            }
            throw new RuntimeException("Error creating user: " + exception.getMessage(), exception);
        }
    }

    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) {
        return firebaseAuthClient.login(userLoginRequest);
    }

    public FirebaseGoogleSignInResponse loginWithGoogle(@NonNull final String idToken) throws FirebaseAuthException {
        FirebaseToken firebaseToken = firebaseAuthClient.verifyToken(idToken);

        return FirebaseGoogleSignInResponse.builder()
                .uid(firebaseToken.getUid())
                .email(firebaseToken.getEmail())
                .build();
    }

    public RefreshTokenSuccessResponse refreshAccessToken(@NonNull final String refreshToken) {
        return firebaseAuthClient.refreshAccessToken(refreshToken);
    }

    public void updateByEmail(@NonNull String email, @NonNull UserUpdateRequest userUpdateRequest) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);

            update(userRecord.getUid(), userUpdateRequest);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
    }

    public void update(@NonNull String uid, @NonNull UserUpdateRequest userUpdateRequest) {
        final var request = new UserRecord.UpdateRequest(uid);

        if (userUpdateRequest.getEmail() != null) {
            request.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getDisplayName() != null) {
            request.setDisplayName(userUpdateRequest.getDisplayName());
        }
        if (userUpdateRequest.getPhoneNumber() != null) {
            request.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        }
        if (userUpdateRequest.getPhotoUrl() != null
                && !Strings.isNullOrEmpty(userUpdateRequest.getPhotoUrl())) {
            request.setPhotoUrl(userUpdateRequest.getPhotoUrl());
        }
        if (userUpdateRequest.getPassword() != null) {
            request.setPassword(userUpdateRequest.getPassword());
        }

        try {
            firebaseAuth.updateUser(request);
        } catch (final Exception exception) {
            throw new RuntimeException("Error updating user: " + exception.getMessage(), exception);
        }
    }
}
