package com.example.identityservice.configuration;

import com.example.identityservice.handler.NotificationWebSocketHandler;
import com.example.identityservice.service.NotificationService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public WebSocketConfig(ApplicationEventPublisher eventPublisher, NotificationWebSocketHandler notificationWebSocketHandler) {
        this.eventPublisher = eventPublisher;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .setAllowedOrigins("*");
    }
}