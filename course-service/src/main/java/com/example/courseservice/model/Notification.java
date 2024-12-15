package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.notificationID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

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
    @EmbeddedId
    notificationID notification_id;

    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    @CreationTimestamp
    Instant notified_date;

}
