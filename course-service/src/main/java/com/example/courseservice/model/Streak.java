package com.example.courseservice.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"streak_records\"")
public class Streak {
    @Id
    @JoinColumn(name = "user_id")
    UUID userId;

    @Column(name = "streak_score")
    Integer streakScore;

    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @UpdateTimestamp
    @Column(name = "last_access")
    Instant lastAccess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medal_id")
    Medal medal;

}
