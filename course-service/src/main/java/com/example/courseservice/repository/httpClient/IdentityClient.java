package com.example.courseservice.repository.httpClient;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.IntrospectRequest;
import com.example.courseservice.dto.response.IntrospectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "identity-service", url = "http://localhost:8001/identity") //"${feign.client.config.identity-service.url}"
public interface IdentityClient {
    @PostMapping(value ="/auth/introspect", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
