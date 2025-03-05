package com.example.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    private String id;
    private String title;
    private String message;
    private NotificationType type;
    private LocalDateTime timestamp;
    private String recipientId; // Can be null for broadcast notifications

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
}