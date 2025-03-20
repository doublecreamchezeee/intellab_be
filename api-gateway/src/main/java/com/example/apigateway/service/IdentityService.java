package com.example.apigateway.service;

import com.example.apigateway.client.IdentityClient;
import com.example.apigateway.dto.response.ValidatedTokenResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {
    IdentityClient identityClient;

    public Mono<ResponseEntity<ValidatedTokenResponse>> validateToken(String token) {
        return identityClient.validateToken(token);
    }

}
