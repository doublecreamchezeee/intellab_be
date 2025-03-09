package com.example.identityservice.service;

import com.example.identityservice.dto.response.notification.NotificationResponse;
import com.example.identityservice.event.WebSocketEvent;
import com.example.identityservice.handler.NotificationWebSocketHandler;
import com.example.identityservice.mapper.NotificationMapper;
import com.example.identityservice.model.Notification;
import com.example.identityservice.repository.NotificationRepository;
import com.example.identityservice.utility.ParseUUID;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public Page<NotificationResponse> fetchNotifications(Pageable pageable, UUID userId) {
        Page<Notification> userNotification = notificationRepository.findAllByRecipientIdOrRecipientIdIsNull(userId,pageable);

        return userNotification.map(notificationMapper::toNotificationResponse);
    }

    public void postNotification(String title, String body, Notification.NotificationType type, UUID userId) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(body);
        notification.setType(type);
        notification.setRecipientId(userId);
        Optional<WebSocketSession> userSession = notificationWebSocketHandler.getUserSessionIfConnected(String.valueOf(userId));
        if (userSession.isPresent()){
            notificationWebSocketHandler.sendNotification(body, userSession.get());
            notification.setMarkAsRead(true);
        } else {
            notification.setMarkAsRead(false);
        }
        notificationRepository.save(notification);
    }

    @EventListener
    public void fetchNotificationsUnread(WebSocketEvent event) {
        String userId = event.getUserId();
        WebSocketSession session = event.getSession();
        List<Notification> unreadNotifications = notificationRepository.findAllByRecipientIdAndMarkAsRead(UUID.fromString(userId), false);

        for (Notification notification : unreadNotifications) {
            try {
                session.sendMessage(new TextMessage(notification.getMessage()));
            } catch (IOException e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        }
            // Mark notifications as read after sending
            unreadNotifications.forEach(notification -> notification.setMarkAsRead(true));
            notificationRepository.saveAll(unreadNotifications);
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
