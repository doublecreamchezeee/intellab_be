package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.achivievementID;
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
@Table(name = "achievements")
public class Achievement {
    @EmbeddedId
    achivievementID achievement_id;

    @CreationTimestamp
    Instant achieved_date;


    @MapsId("medal_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medal_id")
    Medal medal;

}
