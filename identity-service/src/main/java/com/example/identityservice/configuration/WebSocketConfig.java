package com.example.identityservice.configuration;

import com.example.identityservice.handler.NotificationWebSocketHandler;
import com.example.identityservice.service.NotificationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final NotificationService notificationService;

    public WebSocketConfig(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(notificationService), "/ws/notification")
                .setAllowedOrigins("*");
    }
}