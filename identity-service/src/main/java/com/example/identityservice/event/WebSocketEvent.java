package com.example.identityservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Getter
public class WebSocketEvent extends ApplicationEvent {
    private final String userId;
    private final WebSocketSession session;

    public WebSocketEvent(Object source, String userId, WebSocketSession session) {
        super(source);
        this.userId = userId;
        this.session = session;
    }

}