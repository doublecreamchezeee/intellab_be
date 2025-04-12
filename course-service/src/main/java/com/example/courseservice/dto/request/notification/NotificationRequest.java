package com.example.courseservice.dto.request.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String title;
    String message;
    NotificationType type;
    UUID userid;
    String redirectType;
    String redirectContent; 
    String timestamp;


    @Getter
    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        BROADCAST
    }
}
