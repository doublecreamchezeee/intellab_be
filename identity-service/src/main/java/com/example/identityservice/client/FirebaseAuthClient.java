package com.example.identityservice.client;

import com.example.identityservice.configuration.firebase.FirebaseConfigurationProperties;
import com.example.identityservice.dto.request.auth.FirebaseSignInRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.response.auth.FirebaseSignInResponse;
import com.example.identityservice.dto.response.auth.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.auth.TokenSuccessResponse;
import com.example.identityservice.exception.FirebaseAuthenticationException;
import com.example.identityservice.exception.InvalidLoginCredentialsException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.people.v1.PeopleService;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.google.firebase.auth.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseConfigurationProperties.class)
public class FirebaseAuthClient {

    private final FirebaseConfigurationProperties firebaseConfigurationProperties;

    private static final String BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword";
    private static final String API_KEY_PARAM = "key";
    private static final String INVALID_CREDENTIALS_ERROR = "INVALID_LOGIN_CREDENTIALS";
    private static final String REFRESH_TOKEN_URL = "https://securetoken.googleapis.com/v1/token";
    private final FirebaseAuth firebaseAuth;

    @Value("${domain.auth.reset-password-callback-url}")
    private String RESET_PASSWORD_CALLBACK_URL;

    @Value("${domain.auth.email-verification-callback-url}")
    private String EMAIL_VERIFICATION_CALLBACK_URL;

    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) {
        final var requestBody = prepareRequestBody(userLoginRequest);
        final var response = sendSignInRequest(requestBody);
        return TokenSuccessResponse.builder()
                .accessToken(response.getIdToken())
                .refreshToken(response.getRefreshToken())
                .build();
    }

    public FirebaseToken verifyToken(@NonNull final String idToken) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(idToken);
    }

    private FirebaseSignInResponse sendSignInRequest(@NonNull final FirebaseSignInRequest request) {
        final var webApiKey = firebaseConfigurationProperties.getFirebase().getWebApiKey();
        try {
            return RestClient.create(BASE_URL)
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam(API_KEY_PARAM, webApiKey)
                            .build())
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(FirebaseSignInResponse.class);
        } catch (HttpClientErrorException exception) {
            if (exception.getResponseBodyAsString().contains(INVALID_CREDENTIALS_ERROR)) {
                throw new InvalidLoginCredentialsException("Failed to refresh token.", exception);
            }
            throw exception;
        }
    }

    private FirebaseSignInRequest prepareRequestBody(@NonNull final UserLoginRequest userLoginRequest) {
        final var request = new FirebaseSignInRequest();
        request.setEmail(userLoginRequest.getEmail());
        request.setPassword(userLoginRequest.getPassword());
        request.setReturnSecureToken(Boolean.TRUE);
        return request;
    }

    public RefreshTokenSuccessResponse refreshAccessToken(@NonNull final String refreshToken) {
        final var webApiKey = firebaseConfigurationProperties.getFirebase().getWebApiKey();
        final var requestBody = Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken
        );

        System.out.println("Sending request with refreshToken: " + refreshToken);

        try {
            final var response = RestClient.create(REFRESH_TOKEN_URL)
                    .post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", webApiKey).build())
                    .body(requestBody)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(RefreshTokenSuccessResponse.class);


            return RefreshTokenSuccessResponse.builder()
                    .id_token(Objects.requireNonNull(response).getId_token())
                    .refresh_token(response.getRefresh_token())
                    .expires_in(response.getExpires_in())
                    .build();
        } catch (HttpClientErrorException exception) {
            System.err.println("Error refreshing token: " + exception.getResponseBodyAsString());
            throw new InvalidLoginCredentialsException("Failed to refresh token.", exception);
        }
    }

    public String generateEmailVerification(@NonNull final String email) {
        try {
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.builder()
                    .setUrl(EMAIL_VERIFICATION_CALLBACK_URL)
                    .setHandleCodeInApp(true)
                    .setIosBundleId("com.example.ios")
                    .setAndroidPackageName("com.example.android")
                    .setAndroidInstallApp(true)
                    .setAndroidMinimumVersion("12")
                    .setDynamicLinkDomain("example.page.link")
                    .build();

            return firebaseAuth.generateEmailVerificationLink(email);
        } catch (Exception e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public String generatePasswordResetLink(@NonNull final String email) {
        try {
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.builder()
                    .setUrl(RESET_PASSWORD_CALLBACK_URL)
                    .setHandleCodeInApp(true)
                    .setIosBundleId("com.example.ios")
                    .setAndroidPackageName("com.example.android")
                    .setAndroidInstallApp(true)
                    .setAndroidMinimumVersion("12")
                    .setDynamicLinkDomain("example.page.link")
                    .build();

            return firebaseAuth.generatePasswordResetLink(email);
        } catch (Exception e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public void updateUserProfilePicture(String uid, String photoUrl) throws FirebaseAuthException {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setPhotoUrl(photoUrl);

        firebaseAuth.updateUser(request);
    }

    public Boolean isEmailVerified(String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            //UserRecord userRecord = firebaseAuth.getUser(uid);
            return userRecord.isEmailVerified();
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthenticationException();
        }
    }

    public void setVerifiedListEmails(List<String> emails) {
        for (String email : emails) {
            try {
                UserRecord userRecord = firebaseAuth.getUserByEmail(email);

                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                        .setEmailVerified(true);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                throw new FirebaseAuthenticationException();
            }
        }
    }
}