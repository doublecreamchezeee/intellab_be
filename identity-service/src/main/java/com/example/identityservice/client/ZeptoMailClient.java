package com.example.identityservice.client;

import com.example.identityservice.dto.request.zeptomail.SendingEmailRequest;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Component
@FeignClient(name = "zeptomail-service", url = "${zeptomail.api.url}")
public interface ZeptoMailClient {
    @Headers({"Authorization:${zeptomail.auth-token}"}) //, "Content-Type: application/json"}
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) //, headers = {"Authorization:Zoho-enczapikey ${zeptomail.auth-token}"}
    String sendEmail(@RequestBody SendingEmailRequest request, @RequestHeader("Authorization") String authToken); //, @RequestHeader("Content-Type") String contentType
}
