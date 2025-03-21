<<<<<<< HEAD:course-service/src/main/java/com/example/courseservice/model/Leaderboard.java
package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"leaderboard\"")
public class Leaderboard {

    @Id
    @JoinColumn(name = "user_id")
    UUID userId;

    Integer rank;

    @Column(columnDefinition = "VARCHAR(20)")
    String hierarchy;

    Long score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medal_id")
    Medal medal;



}
=======
package com.example.identityservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"leaderboard\"")
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID leaderboardId;

    @JoinColumn(name = "user_id")
    UUID userId;

    String type; // "problem", "course", hoặc "merged"

    Long score; // Tổng điểm

    @Embedded
    ProblemStat problemStat;

    @Embedded
    CourseStat courseStat;
}
>>>>>>> origin/GRAD-553-leaderboard-service-implement:identity-service/src/main/java/com/example/identityservice/model/Leaderboard.java
