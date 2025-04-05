package com.example.identityservice.controller;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.configuration.RedirectUrlConfig;
import com.example.identityservice.dto.request.auth.ListEmailsRequest;
import com.example.identityservice.dto.request.auth.UserCreationRequest;
import com.example.identityservice.dto.request.auth.UserLoginRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final FirestoreService firestoreService;
    private final FirebaseAuthClient firebaseAuthClient;
    private final RedirectUrlConfig redirectUrlConfig;
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

    @Operation(
            summary = "(BE only) callback set verified email",
            description = "callback set verified email from gmail, using redirect URL",
            hidden = true
    )
    @PublicEndpoint
    @GetMapping("/callback-set-verified-email")
    public RedirectView callbackSetVerifiedEmail(@RequestParam String email) {
        log.info("Callback set verified email: {}", email);
        authService.setVerifiedEmail(email);
        return new RedirectView(redirectUrlConfig.getUpdateAccessTokenUrl());
    }

    @Operation(
            summary = "Resend verification email"

    )
    @PublicEndpoint
    @PostMapping("/resend-verification-email")
    public ResponseEntity<HttpStatus> resendVerificationEmail(@RequestBody String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "(testing only) set unverified email"
    )
    @PublicEndpoint
    @PostMapping("/set-unverified-email")
    public ResponseEntity<HttpStatus> setUnverifiedEmail(@RequestBody ListEmailsRequest request) {
        authService.setUnverifiedListEmails(request.getEmails());
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

    @Operation(
            summary = "(testing only) Get is email verified by user uid"
    )
    @PublicEndpoint
    @GetMapping(value = "/is-email-verified")
    public ResponseEntity<Boolean> isVerifiedEmail(@RequestParam String uid) {
        Boolean response = firebaseAuthClient.isEmailVerifiedByUserUid(uid);
        return ResponseEntity.ok(response);
    }
}
