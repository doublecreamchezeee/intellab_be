package com.example.notificationservice.controller;

import com.example.notificationservice.model.Notification;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * API này chỉ là một cách để kích hoạt việc gửi thông báo toàn cục qua WebSocket
     * từ các hệ thống khác hoặc từ giao diện người dùng
     */
    @PostMapping("/global")
    public ResponseEntity<Map<String, String>> sendGlobalNotification(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "INFO") Notification.NotificationType type) {

        // Gọi service để gửi thông báo qua WebSocket
        notificationService.sendGlobalNotification(title, message, type);
        return ResponseEntity.ok(Map.of("status", "Thông báo đã được gửi thành công qua WebSocket"));
    }

    /**
     * API này chỉ là một cách để kích hoạt việc gửi thông báo riêng tư qua WebSocket
     * từ các hệ thống khác hoặc từ giao diện người dùng
     */
    @PostMapping("/private/{recipientId}")
    public ResponseEntity<Map<String, String>> sendPrivateNotification(
            @PathVariable String recipientId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "INFO") Notification.NotificationType type) {

        // Gọi service để gửi thông báo qua WebSocket
        notificationService.sendPrivateNotification(recipientId, title, message, type);
        return ResponseEntity.ok(Map.of("status", "Thông báo đã được gửi thành công đến " + recipientId + " qua WebSocket"));
    }
}

