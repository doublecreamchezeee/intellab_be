package com.example.identityservice.service;

import com.example.identityservice.dto.request.notification.NotificationRequest;
import com.example.identityservice.dto.response.notification.NotificationResponse;
import com.example.identityservice.event.WebSocketEvent;
import com.example.identityservice.handler.NotificationWebSocketHandler;
import com.example.identityservice.mapper.NotificationMapper;
import com.example.identityservice.model.Notification;
import com.example.identityservice.model.User;
import com.example.identityservice.repository.NotificationRepository;
import com.example.identityservice.utility.ParseUUID;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final FirestoreService firestoreService;

    public Page<NotificationResponse> fetchNotifications(Pageable pageable, UUID userId) {
        System.out.println("fetch noti."+userId);
        Page<Notification> userNotification = notificationRepository.findAllByRecipientId(userId,pageable);

        return userNotification.map(notificationMapper::toNotificationResponse);
    }

    public NotificationResponse postNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRecipientId(request.getUserid());
        notification.setMarkAsRead(false);
        notification.setRedirectType(request.getRedirectType());
        notification.setRedirectContent(request.getRedirectContent());
        
        final Notification savedNotification = notificationRepository.save(notification);

        Optional<WebSocketSession> userSession = notificationWebSocketHandler.getUserSessionIfConnected(request.getUserid());
        // userSession.ifPresent(webSocketSession
        //         -> notificationWebSocketHandler.sendNotification(title + "\n" +body, webSocketSession));

        userSession.ifPresent(webSocketSession
                   -> notificationWebSocketHandler.sendNotification(savedNotification, webSocketSession));
        return notificationMapper.toNotificationResponse(savedNotification);
    }

    @EventListener
    public void fetchNotificationsUnread(WebSocketEvent event) {
        String userId = event.getUserId();
        WebSocketSession session = event.getSession();
        List<Notification> unreadNotifications = notificationRepository.findAllByRecipientIdAndMarkAsRead(UUID.fromString(userId), false);

        for (Notification notification : unreadNotifications) {
                notificationWebSocketHandler.sendNotification(notification, session);
                // session.sendMessage(new TextMessage(notification.getMessage()));
        }
            // Mark notifications as read after sending
            notificationRepository.saveAll(unreadNotifications);
        }

    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            System.err.println("Notification not found: " + notificationId);
            return null;
        }
        notification.setMarkAsRead(true);
        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }

    public Page<NotificationResponse> markAsReadAll(Pageable pageable, UUID userId) {
        Page<Notification> userNotification = notificationRepository.findAllByRecipientId(userId,pageable);

        for (Notification notification : userNotification.getContent()) {
            notification.setMarkAsRead(true);
            notificationRepository.save(notification);
        }
        return userNotification.map(notificationMapper::toNotificationResponse);
    }

    public NotificationResponse broadcastNotification(String title, String body, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(body);
        notification.setType(type);
        notification.setMarkAsRead(false);
        List<User> users = new ArrayList<>();
        try {
            users = firestoreService.getAllUsers();
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Failed to broadcast notification-while get all users: " + e.getMessage());
        }
        for (User user:users)
        {
            notification.setRecipientId(ParseUUID.normalizeUID(user.getUid()));
            notificationRepository.save(notification);
        }
        notification.setRecipientId(null);
        return notificationMapper.toNotificationResponse(notification);
    }


}
