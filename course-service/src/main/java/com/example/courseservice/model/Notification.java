package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.NotificationID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"notifications\"")
public class Notification {
    @EmbeddedId
    NotificationID notificationId;

    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    @CreationTimestamp
    @Column(name = "notified_date")
    Instant notifiedDate;

}
