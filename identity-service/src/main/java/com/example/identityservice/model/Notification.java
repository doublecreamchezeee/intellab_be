package com.example.identityservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"notifications\"")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String redirectType;

    private String redirectContent;
    
    private Notification.NotificationType type;

    @CreationTimestamp
    private Timestamp timestamp;

    private Boolean markAsRead;

    private UUID recipientId;

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

}
