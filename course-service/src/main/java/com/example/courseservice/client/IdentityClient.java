package com.example.courseservice.client;

import com.example.courseservice.dto.response.auth.ValidatedTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "identity-service", url = "http://localhost:8001/identity")//"${identity-service.url}"
public interface IdentityClient {
    @PostMapping(value = "/auth/validateToken", consumes = MediaType.APPLICATION_JSON_VALUE)
    ValidatedTokenResponse validateToken(String token);
}
