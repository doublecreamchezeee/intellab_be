package com.example.courseservice.client;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.notification.NotificationRequest;
import com.example.courseservice.dto.request.LeaderboardUpdateRequest;
import com.example.courseservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.courseservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.courseservice.dto.response.auth.ValidatedTokenResponse;
import com.example.courseservice.dto.response.notification.NotificationResponse;
import com.example.courseservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.courseservice.dto.response.profile.SingleProfileInformationResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/validateToken", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<ValidatedTokenResponse>> validateToken(@RequestBody String token);

    @PostExchange(url = "/profile/single/public", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<SingleProfileInformationResponse>> getSingleProfileInformation(
            @RequestBody SingleProfileInformationRequest request);

    @PostExchange(url = "/profile/multiple", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<MultipleProfileInformationResponse>> getMultipleProfileInformation(
            @RequestBody MultipleProfileInformationRequest request);

    @PostExchange(url = "/leaderboard/update", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> updateLeaderboard(@RequestBody LeaderboardUpdateRequest request);

//    @GetExchange(url = "/notifications")
//    Mono<ApiResponse<Page<NotificationResponse>>> getNotifications(
//            @RequestHeader("X-UserId") String userUid,
//            @ParameterObject Pageable pageable
//    );
    @PostExchange(url = "/notifications")
    Mono<ApiResponse<NotificationResponse>> postNotifications(
            @RequestBody NotificationRequest request
    );


}