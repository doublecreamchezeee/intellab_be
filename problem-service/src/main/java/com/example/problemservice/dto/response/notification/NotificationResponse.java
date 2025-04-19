package com.example.problemservice.dto.response.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    UUID id;

    String title;

    String message;

    String redirectType;

    String redirectContent;

    NotificationResponse.NotificationType type;

    Date timestamp;

    Boolean markAsRead;

    UUID recipientId;

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        BROADCAST
    }
}