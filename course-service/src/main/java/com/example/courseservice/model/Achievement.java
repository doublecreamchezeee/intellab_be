package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.AchievementID;
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
@Table(name = "achievements")
public class Achievement {
    @EmbeddedId
    AchievementID achievementId;

    @CreationTimestamp
    @Column(name = "achieved_date")
    Instant achievedDate;


//    @MapsId("medalId")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "medal_id")
//    Medal medal;

}
