package com.example.notificationservice.service;

import com.example.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a notification to all connected clients
     */
    public void sendGlobalNotification(String title, String message, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .type(type)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Sending global notification: {}", notification);
        messagingTemplate.convertAndSend("/all/notifications", notification);
    }

    /**
     * Send a notification to a specific user
     */
    public void sendPrivateNotification(String recipientId, String title, String message, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .type(type)
                .timestamp(LocalDateTime.now())
                .recipientId(recipientId)
                .build();

        log.info("Sending private notification to {}: {}", recipientId, notification);
        messagingTemplate.convertAndSend("/specific/notifications/" + recipientId, notification);
    }



}

