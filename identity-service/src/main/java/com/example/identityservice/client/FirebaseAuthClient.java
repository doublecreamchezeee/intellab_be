package com.example.identityservice.client;

import com.example.identityservice.configuration.firebase.FirebaseConfigurationProperties;
import com.example.identityservice.dto.request.auth.FirebaseSignInRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.response.auth.FirebaseSignInResponse;
import com.example.identityservice.dto.response.auth.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.auth.TokenSuccessResponse;
import com.example.identityservice.exception.InvalidLoginCredentialsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

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
    private final FirestoreClient firestoreClient;

    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) throws InvalidLoginCredentialsException {
        // Step 1: Prepare the request body and send the sign-in request to Firebase
        final var requestBody = prepareRequestBody(userLoginRequest);
        final var response = sendSignInRequest(requestBody);

        // Step 2: Verify the ID token obtained from Firebase
        FirebaseToken firebaseToken;
        try {
            firebaseToken = firebaseAuth.verifyIdToken(response.getIdToken());
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to verify token", e);
        }

        // Step 3: Retrieve the user's role from Firestore based on the UID
        String role = firestoreClient.getRole(firebaseToken.getUid());  // Assume you have a method to get the role

        // Step 4: Set the custom claim 'role' for the user
        try {
            firebaseAuth.setCustomUserClaims(firebaseToken.getUid(), Map.of("role", role));
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to set custom claims", e);
        }

        // Step 5: Return the response with the access token and refresh token
        return TokenSuccessResponse.builder()
                .accessToken(response.getIdToken())  // The token with the updated claims
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

    public RefreshTokenSuccessResponse refreshAccessToken(@NonNull final String refreshToken) throws InvalidLoginCredentialsException {
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
                    .id_token(response.getId_token())
                    .refresh_token(response.getRefresh_token())
                    .expires_in(response.getExpires_in())
                    .build();
        } catch (HttpClientErrorException exception) {
            System.err.println("Error refreshing token: " + exception.getResponseBodyAsString());
            throw new InvalidLoginCredentialsException("Failed to refresh token.", exception);
        }
    }
}