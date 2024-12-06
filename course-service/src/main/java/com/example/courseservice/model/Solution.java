package com.example.courseservice.model;



import com.example.courseservice.model.compositeKey.solutionID;
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


    @ManyToOne
    @MapsId("problem_id")
    @JoinColumn(name = "problem_id", nullable = false)
    Problem problem;


    @MapsId("author_id")
    @JoinColumn(name = "author_id")
    UUID user_id;
}
