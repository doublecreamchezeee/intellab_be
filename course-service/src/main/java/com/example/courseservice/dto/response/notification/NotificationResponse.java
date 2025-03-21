package com.example.courseservice.dto.response.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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

    NotificationResponse.NotificationType type;

    Date timestamp;

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
}