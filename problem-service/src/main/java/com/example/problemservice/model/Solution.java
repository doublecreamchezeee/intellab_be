package com.example.problemservice.model;

import com.example.problemservice.model.composite.SolutionID;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "\"Solutions\"")
public class Solution {
    @EmbeddedId
    SolutionID solutionId;

    @Column(columnDefinition = "TEXT")
    String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("problemId")
    @JoinColumn(name = "problem_id", nullable = false)
    @JsonBackReference("problem-solution")
    Problem problem;

    @MapsId("user_id")
    @JoinColumn(name = "user_id")
    UUID userId;
}