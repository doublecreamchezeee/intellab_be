package com.example.identityservice.handler;

import com.example.identityservice.dto.response.notification.NotificationResponse;
import com.example.identityservice.event.WebSocketEvent;
import com.example.identityservice.model.Notification;
import com.example.identityservice.service.NotificationService;
import com.example.identityservice.utility.ParseUUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public NotificationWebSocketHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public Optional<WebSocketSession> getUserSessionIfConnected(UUID userId) {
        WebSocketSession session = sessions.get(userId.toString());
        if (session != null && session.isOpen()) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            System.out.println("User connected: " + userId);
            sendWelcomeMessage(UUID.fromString(userId));

            eventPublisher.publishEvent(new WebSocketEvent(this, userId, session));
        }
    }

//    private void sendNotification(Page<NotificationResponse> notificationResponses, String userId) {
//        WebSocketSession session = sessions.get(userId);
//
//        if (session != null && session.isOpen()) {
//            for (NotificationResponse notification : notificationResponses) {
//                try {
//                    session.sendMessage(new TextMessage(notification.getMessage()));
//                } catch (IOException e) {
//                    System.err.println("Failed to send message: " + e.getMessage());
//                }
//            }
//        }
//    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            System.out.println("User disconnected: " + userId);
        }
    }

    private void sendWelcomeMessage(UUID userId) {
        WebSocketSession session = sessions.get(userId.toString());
        if (session != null && session.isOpen()) {
            try {
                String welcomeMessage = "Welcome " + userId + ", you are now connected!";
                session.sendMessage(new TextMessage(welcomeMessage));
            } catch (IOException e) {
                System.err.println("Failed to send welcome message: " + e.getMessage());
            }
        }
    }

    public void sendNotification(String message, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNotification(Notification notification, WebSocketSession session){
        try {
            ObjectMapper notificationObject = new ObjectMapper();
            String json = notificationObject.writeValueAsString(notification);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send WebSocket message", e);
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return query.substring(7);
        }
        return null;
    }

    public void broadcastMessage(String title, String message, Notification.NotificationType type) {
        // lưu db để các tài khoản ofline thì khi online lại có thẻ fetch lên để xem
//        notificationService.broadcastNotification(title, message, type);
//
//        for (WebSocketSession session : sessions.values()) {
//            if (session.isOpen()) {
//                try {
//                    session.sendMessage(new TextMessage(message));
//                } catch (IOException e) {
//                    System.err.println("Failed to send broadcast message: " + e.getMessage());
//                }
//            }
//        }
    }

}

        //              [Event] -- gọi đâu ta
        //                |
        //                v
        //        +--------------------+
        //        |NotificationService |
        //        +--------------------+
    //                       |
    //        +---------------------------+
    //        |      Notification DB      |
    //        +---------------------------+
    //                |               |
    //        +-----------+       | nếu có thì sau sẽ thêm vào gửi email nữa |
    //        |  WebSocket |
    //        +-----------+
    //                |
//        +---------------------------+
//        | User Device (Web/Mobile)  |
//        +---------------------------+
