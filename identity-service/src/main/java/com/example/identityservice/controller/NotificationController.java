package com.example.identityservice.controller;


import com.example.identityservice.configuration.PublicEndpoint;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.notification.NotificationRequest;
import com.example.identityservice.dto.response.notification.NotificationResponse;
import com.example.identityservice.service.NotificationService;
import com.example.identityservice.utility.ParseUUID;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.example.identityservice.model.Notification.NotificationType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "Notification")
public class NotificationController {
    private final NotificationService notificationService;

    @PublicEndpoint
    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(@RequestBody NotificationRequest request) {
        // nếu userid = null thì broadcast
        if (request.getUserid() == null) {
            return ApiResponse.<NotificationResponse>builder()
                    .result(notificationService.broadcastNotification(
                            request.getTitle(),
                            request.getMessage(),
                            INFO))
                    .build();
        }
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.postNotification(
                        request.getTitle()
                        ,request.getMessage()
                        ,INFO
                        ,request.getUserid()))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> fetchNotifications(
            @RequestHeader("X-UserId") String userUid,
            @ParameterObject Pageable pageable) {
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.fetchNotifications(pageable, userId)).build();
    }

//    @GetMapping("/fetchUnread")
//    public ApiResponse<Page<NotificationResponse>> fetchUnreadNotifications(
//            @RequestHeader("X-UserId") String userUid,
//            @ParameterObject Pageable pageable){
//        return ApiResponse.<Page<NotificationResponse>>builder()
//                .result(notificationService.fetchNotificationsUnread();).build();
//    }

    @PutMapping("/markAsRead")
    public ApiResponse<NotificationResponse> markAsRead(@RequestParam UUID notificationId) {
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.markAsRead(notificationId)).build();
    }

    @PutMapping("/markAllAsRead")
    public ApiResponse<Page<NotificationResponse>> markAllAsRead(
            @RequestHeader("X-UserId") String userUid,
            @ParameterObject Pageable pageable) {
        userUid = userUid.split(",")[0];

        UUID userId = ParseUUID.normalizeUID(userUid);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.markAsReadAll(pageable,userId)).build();
    }

}
