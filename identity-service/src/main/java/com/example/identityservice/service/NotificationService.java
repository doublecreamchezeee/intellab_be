package com.example.identityservice.service;

import com.example.identityservice.dto.response.notification.NotificationResponse;
import com.example.identityservice.handler.NotificationWebSocketHandler;
import com.example.identityservice.mapper.NotificationMapper;
import com.example.identityservice.model.Notification;
import com.example.identityservice.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public Page<NotificationResponse> fetchNotifications(Pageable pageable, UUID userId) {
        Page<Notification> userNotification = notificationRepository.findAllByRecipientIdOrRecipientIdIsNull(userId,pageable);

        return userNotification.map(notificationMapper::toNotificationResponse);
    }

    public NotificationResponse postNotification(String title, String body, Notification.NotificationType type, UUID userId) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(body);
        notification.setType(type);
        notification.setRecipientId(userId);
        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }

    public NotificationResponse broadcastNotification(String title, String body, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(body);
        notification.setType(type);
        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }


}
