package com.example.identityservice.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"badges\"")
public class Badge {
    @Id
    @GeneratedValue
    @Column(name = "badge_id")
    Integer badgeId;

    @Column(name = "badge_name", nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String image;

    @Column(columnDefinition = "VARCHAR(20)")
    String type;

    @Column(name = "condition")
    String condition;

    @Column(name = "locked_image")
    String lockedImage;

    @OneToMany(mappedBy = "badge")
    List<Achievement> achievementList;

//    @OneToMany(mappedBy = "medal",fetch = FetchType.LAZY)
//    Set<Leaderboard> leaderboards;

//    @OneToMany(mappedBy = "medal",fetch = FetchType.LAZY)
//    Set<Streak> streaks;

//    @OneToMany(mappedBy = "medal",fetch = FetchType.LAZY)
//    List<Achievement> achievementList;
}
