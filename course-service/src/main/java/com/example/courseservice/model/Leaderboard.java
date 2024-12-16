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
    UUID user_id;

    Integer rank;

    @Column(columnDefinition = "VARCHAR(20)")
    String hierarchy;
    Long Score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medal_id")
    Medal medal;



}