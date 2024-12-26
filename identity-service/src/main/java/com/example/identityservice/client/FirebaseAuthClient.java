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
import com.google.firebase.auth.UserRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
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

    @SneakyThrows
    public TokenSuccessResponse login(@NonNull final UserLoginRequest userLoginRequest) throws InvalidLoginCredentialsException {
        try {
            // Step 1: Get the user from Firebase Authentication by email
            UserRecord user = firebaseAuth.getUserByEmail(userLoginRequest.getEmail());

            // Step 2: Retrieve the user's role from Firestore (or another source)
            String role = firestoreClient.getRole(user.getUid());

            // Step 3: Set custom claims for the user (role)
            Map<String, Object> customClaims = new HashMap<>();
            customClaims.put("role", role);

            // Update user's custom claims
            firebaseAuth.setCustomUserClaims(user.getUid(), customClaims);

            // Step 4: Prepare the login request body and send the sign-in request
            var requestBody = prepareRequestBody(userLoginRequest);
        
            var response = sendSignInRequest(requestBody);
            // Step 6: Return the response with the access token and refresh token
            return TokenSuccessResponse.builder()
                    .accessToken(response.getIdToken())  // The token with updated claims
                    .refreshToken(response.getRefreshToken())
                    .build();

        } catch (InvalidLoginCredentialsException e) {
            // Catch any other unexpected exceptions and throw an appropriate exception
        throw new InvalidLoginCredentialsException("Invalid login credentials: " + e.getMessage(), e);
        }
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
            String errorMessage = exception.getResponseBodyAsString();
            if (errorMessage.contains(INVALID_CREDENTIALS_ERROR)) {
                throw new InvalidLoginCredentialsException("Invalid login credentials: " + errorMessage, exception);
            }
            // Log the error message if needed
            throw new RuntimeException("Failed to sign in. Error details: " + errorMessage, exception);
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