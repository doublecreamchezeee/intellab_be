package com.example.courseservice.client;

import com.example.courseservice.dto.response.auth.ValidatedTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/validateToken", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<ValidatedTokenResponse>> validateToken(@RequestBody String token);

}