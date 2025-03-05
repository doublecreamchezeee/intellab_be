package com.example.notificationservice.service;

import com.example.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final NotificationService notificationService;

    /**
     * Ví dụ về cách gửi thông báo trực tiếp từ một sự kiện trong hệ thống
     */
    public void createUser(String userId, String email) {
        // Logic tạo người dùng...
        log.info("Tạo người dùng mới: {}, email: {}", userId, email);

        // Sau khi tạo người dùng thành công, gửi thông báo trực tiếp qua WebSocket
        notificationService.sendGlobalNotification(
                "Người dùng mới",
                "Người dùng " + userId + " vừa tham gia hệ thống",
                Notification.NotificationType.INFO
        );

        // Gửi thông báo riêng tư đến người dùng vừa tạo
        notificationService.sendPrivateNotification(
                userId,
                "Chào mừng!",
                "Chào mừng bạn đến với hệ thống của chúng tôi",
                Notification.NotificationType.SUCCESS
        );
    }
}
