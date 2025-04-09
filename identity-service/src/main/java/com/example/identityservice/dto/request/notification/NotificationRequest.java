package com.example.identityservice.dto.request.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

import com.example.identityservice.model.Notification;

import static com.example.identityservice.model.Notification.NotificationType.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String title;
    String message;
    Notification.NotificationType type;
    UUID userid;
    String redirectType;
    String redirectContent; 
    String timestamp;

}
