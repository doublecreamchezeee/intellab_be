package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.identityservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.identityservice.dto.response.auth.FirebaseGoogleSignInResponse;
import com.example.identityservice.dto.response.auth.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.auth.TokenSuccessResponse;
import com.example.identityservice.dto.response.auth.ValidatedTokenResponse;
import com.example.identityservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.identityservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.identityservice.exception.AccountAlreadyExistsException;
import com.example.identityservice.exception.FirebaseAuthenticationException;
import com.example.identityservice.exception.NotVerifiedEmailException;
import com.example.identityservice.exception.SendingEmailFailedException;
import com.example.identityservice.model.User;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseAuthClient firebaseAuthClient;
    private final EmailService emailService;
    private final FirestoreService firestoreService;

    @SneakyThrows
    public void create(@NonNull final UserCreationRequest userCreationRequest) {
        log.info("Creating user with email: {}", userCreationRequest.getEmail());
        final var request = new UserRecord.CreateRequest()
                .setEmail(userCreationRequest.getEmail())
                .setPassword(userCreationRequest.getPassword())
                .setEmailVerified(Boolean.TRUE)
                .setDisplayName(userCreationRequest.getDisplayName())
                .setEmailVerified(Boolean.FALSE);

        try {
            UserRecord userRecord = firebaseAuth.createUser(request);
            log.info("User successfully created: {}", userCreationRequest.getEmail());
            firestoreService.createUserById(userRecord.getUid(), "User", userRecord.getUid());
            firebaseAuth.generatePasswordResetLink(userRecord.getEmail());
            sendEmailVerification(userRecord.getUid());

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
        boolean isEmailVerified = false;
        isEmailVerified = firebaseAuthClient.isEmailVerified(userLoginRequest.getEmail());
        if (!isEmailVerified) {
            throw new NotVerifiedEmailException();
        }
        return firebaseAuthClient.login(userLoginRequest);
    }


    public FirebaseGoogleSignInResponse loginWithGoogle(@NonNull final String idToken) throws FirebaseAuthException {
        ValidatedTokenResponse firebaseToken = firebaseAuthClient.verifyToken(idToken);
        try {
            User user = firestoreService.getUserById(firebaseToken.getUserId());
            if (user == null || user.getFirstName() == null || user.getLastName() == null) {
                firestoreService.createUserById(firebaseToken.getUserId(), "User", firebaseToken.getUserId());
            }
            return FirebaseGoogleSignInResponse.builder()
                    .uid(firebaseToken.getUserId())
                    .email(firebaseToken.getEmail())
                    .build();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

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

        if (userUpdateRequest.getDisplayName() != null) {
            request.setDisplayName(userUpdateRequest.getDisplayName());
        }
        try {
            firestoreService.updateUserById(uid, userUpdateRequest.getFirstName(), userUpdateRequest.getLastName());
            firebaseAuth.updateUser(request);
        } catch (final Exception exception) {
            throw new RuntimeException("Error updating user: " + exception.getMessage(), exception);
        }
    }

    public ValidatedTokenResponse validateToken(@NonNull final String token) {
        try {
            FirebaseToken decodeToken = FirebaseAuth.getInstance().verifyIdToken(token);

            return ValidatedTokenResponse.builder()
                    .isValidated(true)
                    .userId(decodeToken.getUid())
                    .name(decodeToken.getName())
                    .email(decodeToken.getEmail())
                    .message("Token validation successful.")
                    .build();
        } catch (FirebaseAuthException e) {
            return ValidatedTokenResponse.builder()
                    .isValidated(false)
                    .message("Invalid token: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return ValidatedTokenResponse.builder()
                    .isValidated(false)
                    .message("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    public void sendEmailVerification(String uid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            String email = userRecord.getEmail();

            if (email != null) {
                String link = firebaseAuthClient.generateEmailVerification(email);
                System.out.println("generate email to " + email + " with link: " + link);
                emailService.sendMail(
                        email,
                        "Verify your email in Intellab website",
                        "Congrats on sending your confirmation link: " + link);
            }
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public void sendPasswordResetLink(String email) {
        try {
            String link = firebaseAuthClient.generatePasswordResetLink(email);

            emailService.sendMail(
                    email,
                    "Reset your password in Intellab website",
                    "Congrats on sending your password reset link: " + link);
        } catch (Exception e) {
            throw new SendingEmailFailedException();
        }
    }

    public void setVerifiedListEmails(List<String> email) {
        try {
            firebaseAuthClient.setVerifiedListEmails(email);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying email: " + e.getMessage(), e);
        }
    }
}