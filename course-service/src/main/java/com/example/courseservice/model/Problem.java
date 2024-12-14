package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"problems\"")
public class Problem {
    @Id
    @GeneratedValue
    UUID problem_id;

    String problem_name;

    @Lob
    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "VARCHAR(20)")
    String problem_level;

    @Column(columnDefinition = "VARCHAR(50)")
    String category;
    Integer score;

    @Column(columnDefinition = "DECIMAL(5,2)")
    Float acceptance_rate;

    @OneToMany(mappedBy = "problem")
    List<Lesson> lessons;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "topic_id")
    Topic topic;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Hint> hints;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    List<TestCase> testCases;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    List<ProblemSubmission> submissions;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    List<Solution> solutions;

}
