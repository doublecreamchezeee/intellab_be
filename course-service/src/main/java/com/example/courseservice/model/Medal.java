package com.example.courseservice.model;



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
    UUID medal_id;

    @Column(name = "medal_name", nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String image;

    @Column(columnDefinition = "VARCHAR(20)")
    String type;
    Integer bonus_score;

//    @OneToMany(mappedBy = "medal")
//    Set<Leaderboard> leaderboards;
//
//    @OneToMany(mappedBy = "medal")
//    Set<Streak> streaks;

    @OneToMany(mappedBy = "medal")
    List<Achievement> achievementList;
}
