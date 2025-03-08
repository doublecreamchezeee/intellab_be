package com.example.identityservice.dto.response.notification;


import com.example.identityservice.model.Notification;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
