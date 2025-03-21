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
