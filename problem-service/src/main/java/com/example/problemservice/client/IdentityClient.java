package com.example.problemservice.client;

import com.example.problemservice.dto.request.notification.NotificationRequest;
import com.example.problemservice.dto.request.LeaderboardUpdateRequest;
import com.example.problemservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.problemservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.notification.NotificationResponse;
import com.example.problemservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.problemservice.dto.response.profile.SingleProfileInformationResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/profile/single/public", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<SingleProfileInformationResponse>> getSingleProfileInformation(
            @RequestBody SingleProfileInformationRequest request);

    @PostExchange(url = "/profile/single/profile", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<SingleProfileInformationResponse>> getSingleProfileInformationById(
            @RequestBody SingleProfileInformationRequest request);

    @PostExchange(url = "/profile/multiple", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<MultipleProfileInformationResponse>> getMultipleProfileInformation(
            @RequestBody MultipleProfileInformationRequest request);

    @PostExchange(url = "/notifications")
    Mono<ApiResponse<NotificationResponse>> postNotifications(
            @RequestBody NotificationRequest request
    );


    @PostExchange(url = "/leaderboard/update", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<Void>> updateLeaderboard(@RequestBody LeaderboardUpdateRequest request);
}