package com.example.identityservice.handler;

import com.example.identityservice.service.NotificationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    public NotificationWebSocketHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            System.out.println("User connected: " + userId);

            sendWelcomeMessage(userId);

            // Gửi thông báo qua NotificationService
//            Notification notification = notificationService.notifyUsers(ParseUUID.normalizeUID(userId));
//            if (notification != null) {
//                sendNotification(notification, userId);
//            }
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            System.out.println("User disconnected: " + userId);
        }
    }

    private void sendWelcomeMessage(String userId) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String welcomeMessage = "Welcome " + userId + ", you are now connected!";
                session.sendMessage(new TextMessage(welcomeMessage));
            } catch (IOException e) {
                System.err.println("Failed to send welcome message: " + e.getMessage());
            }
        }
    }

//    public void sendNotification(Notification notification, String userId) {
//        WebSocketSession session = sessions.get(userId);
//        if (session != null && session.isOpen()) {
//            try {
//                String message = notification.getMessage();
//                session.sendMessage(new TextMessage(message));
//            } catch (IOException e) {
//                System.err.println("Failed to send message: " + e.getMessage());
//            }
//        }
//    }

    private String getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return query.substring(7);
        }
        return null;
    }
}
