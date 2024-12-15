package com.example.problemservice.model;

import com.example.problemservice.model.composite.solutionID;
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
    solutionID solution_id;

    @Column(columnDefinition = "TEXT")
    String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("problem_id")
    @JoinColumn(name = "problem_id", nullable = false)
    @JsonBackReference("problem-solution")
    Problem problem;


    @MapsId("author_id")
    @JoinColumn(name = "author_id")
    UUID user_id;
}