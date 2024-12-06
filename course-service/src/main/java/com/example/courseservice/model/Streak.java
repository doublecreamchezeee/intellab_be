package com.example.courseservice.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
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
    UUID user_id;

    Integer streak_score;

    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @UpdateTimestamp
    Instant last_access;

    @ManyToOne
    @JoinColumn(name = "medal_id")
    Medal medal;

}
