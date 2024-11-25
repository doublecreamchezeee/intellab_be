package com.example.identityservice.controller;

import com.example.identityservice.dto.request.UserCreationRequest;
import com.example.identityservice.dto.request.UserLoginRequest;
import com.example.identityservice.dto.response.RefreshTokenSuccessResponse;
import com.example.identityservice.dto.response.TokenSuccessResponse;
import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PublicEndpoint
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> createUser(@Valid @RequestBody final UserCreationRequest userCreationRequest) {
        userService.create(userCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PublicEndpoint
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenSuccessResponse> login(@Valid @RequestBody final UserLoginRequest userLoginRequest) {
        final var response = userService.login(userLoginRequest);
        return ResponseEntity.ok(response);
    }

    @PublicEndpoint
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RefreshTokenSuccessResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        RefreshTokenSuccessResponse response = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }

}
