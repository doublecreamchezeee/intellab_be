package com.example.apigateway.client;

import com.example.apigateway.dto.response.ValidatedTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/validateToken", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<ValidatedTokenResponse>> validateToken(@RequestBody String token);
}
