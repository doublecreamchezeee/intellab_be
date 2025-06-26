package com.example.identityservice.model;

import com.example.identityservice.model.composite.AchievementId;
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
@Table(name = "\"achievements\"")
public class Achievement {
    @EmbeddedId
    AchievementId id;

    @CreationTimestamp
    @Column(name = "achieved_date")
    Instant achievedDate;


    @MapsId("badgeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", referencedColumnName = "badge_id")
    Badge badge;
}
