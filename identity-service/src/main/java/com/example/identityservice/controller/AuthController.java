package com.example.identityservice.controller;

import com.example.identityservice.dto.request.auth.ListEmailsRequest;
import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
import com.example.identityservice.dto.request.auth.UserUpdateRequest;
import com.example.identityservice.dto.response.auth.*;
import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.service.AuthService;
import com.example.identityservice.service.FirestoreService;
import com.example.identityservice.utility.SecurityUtil;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final FirestoreService firestoreService;

    @Operation(
            summary = "Register user"
    )
    @PublicEndpoint
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> createUser(@Valid @RequestBody final UserCreationRequest userCreationRequest) {
        authService.create(userCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Login"
    )
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

    @Operation(
            summary = "Login with Google"
    )
    @PublicEndpoint
    @PostMapping(value = "/login/google")
    public ResponseEntity<FirebaseGoogleSignInResponse> loginWithGoogle(@RequestBody final Map<String, String> body) throws FirebaseAuthException {
        String idToken = body.get("idToken");
        FirebaseGoogleSignInResponse response = authService.loginWithGoogle(idToken);
        return ResponseEntity.ok(response);
    }

//    @Operation(
//            summary = "Update user by email"
//    )
//    @PutMapping("/update")
//    public ResponseEntity<HttpStatus> updateUserByEmail(@RequestParam("email") String email, @Validated @RequestBody UserUpdateRequest userUpdateRequest) {
//        authService.updateByEmail(email, userUpdateRequest);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }

    @Operation(
            summary = "Refresh token, re"
    )
    @PublicEndpoint
    @PostMapping(value = "/refresh")
    public ResponseEntity<TokenSuccessResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("token"); // ✅ Extract from JSON
        RefreshTokenSuccessResponse refreshTokenResponse = authService.refreshAccessToken(refreshToken);
        TokenSuccessResponse response = TokenSuccessResponse.builder()
                .accessToken(refreshTokenResponse.getId_token())
                .refreshToken(refreshTokenResponse.getRefresh_token())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validate token, return true if it is valid"
    )
    @PublicEndpoint
    @PostMapping("/validateToken")
    public ResponseEntity<ValidatedTokenResponse> validateToken(@RequestBody String token) {
        ValidatedTokenResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Send password reset link"
    )
    @PublicEndpoint
    @PostMapping("/reset-password")
    public ResponseEntity<HttpStatus> sendPasswordResetLink(@RequestBody String email) {
        authService.sendPasswordResetLink(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "(testing only) set verified email"
    )
    @PublicEndpoint
    @PostMapping("/set-verified-email")
    public ResponseEntity<HttpStatus> setVerifiedEmail(@RequestBody ListEmailsRequest request) {
        authService.setVerifiedListEmails(request.getEmails());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PublicEndpoint
    @GetMapping("/role")
    public ResponseEntity<Collection<? extends GrantedAuthority>> roleEndpoint() {
        return ResponseEntity.ok(SecurityUtil.getUserAuthorities());
    }

    @GetMapping("/premium")
    public PremiumSubscriptionResponse getSubscription(@RequestParam String uid) {
        return authService.getUserPremiumSubscriptionByUid(uid);
    }
}
