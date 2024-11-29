package com.example.identityservice.controller;

import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.response.auth.FirebaseGoogleSignInResponse;
import com.example.identityservice.dto.response.auth.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.auth.TokenSuccessResponse;
import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PublicEndpoint
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> createUser(@Valid @RequestBody final UserCreationRequest userCreationRequest) {
        authService.create(userCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PublicEndpoint
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenSuccessResponse> login(@Valid @RequestBody final UserLoginRequest userLoginRequest, HttpServletResponse response) {
        final var tokenResponse = authService.login(userLoginRequest);

        String refreshToken = tokenResponse.getRefreshToken();

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Use secure flag in production
        refreshTokenCookie.setPath("/"); // Make the cookie available site-wide
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 1 week expiration
        response.addCookie(refreshTokenCookie);


        return ResponseEntity.ok(tokenResponse);
    }

    @PublicEndpoint
    @PostMapping(value = "/login/google")
    public ResponseEntity<FirebaseGoogleSignInResponse> loginWithGoogle(@RequestBody final Map<String, String> body) throws FirebaseAuthException {
        String idToken = body.get("idToken");
        FirebaseGoogleSignInResponse response = authService.loginWithGoogle(idToken);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<HttpStatus> updateUserByEmail(@RequestParam("email") String email, @Validated @RequestBody UserUpdateRequest userUpdateRequest) {
        authService.updateByEmail(email, userUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PublicEndpoint
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenSuccessResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        RefreshTokenSuccessResponse refreshTokenResponse = authService.refreshAccessToken(refreshToken);
        TokenSuccessResponse response = TokenSuccessResponse.builder()
                .accessToken(refreshTokenResponse.getId_token())
                .refreshToken(refreshTokenResponse.getRefresh_token())
                .build();
        return ResponseEntity.ok(response);
    }

}
