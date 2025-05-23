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
@Table(name = "\"medals\"")
public class Medal {
    @Id
    @GeneratedValue
    @Column(name = "medal_id")
    UUID medalId;

    @Column(name = "medal_name", nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String image;

    @Column(columnDefinition = "VARCHAR(20)")
    String type;
    @Column(name = "bonus_score")
    Integer bonusScore;

//    @OneToMany(mappedBy = "medal",fetch = FetchType.LAZY)
//    Set<Leaderboard> leaderboards;

//    @OneToMany(mappedBy = "medal",fetch = FetchType.LAZY)
//    Set<Streak> streaks;

//    @OneToMany(mappedBy = "medal",fetch = FetchType.LAZY)
//    List<Achievement> achievementList;
}
