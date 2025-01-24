package com.example.problemservice.model;

import com.fasterxml.jackson.annotation.*;
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
    @Column(name = "problem_id")
    UUID problemId;

    @Column(name = "problem_name")
    String problemName;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "problem_level", columnDefinition = "VARCHAR(20)")
    String problemLevel;

//    @Column(columnDefinition = "VARCHAR(50)")
//    String category;

    Integer score;

    @Column(name = "acceptance_rate", columnDefinition = "DECIMAL(5,2)")
    Float acceptanceRate;

    @Column(name = "is_available")
    Boolean isAvailable;

    @Column(name = "is_published")
    Boolean isPublished;

    @Column(name = "problem_structure", columnDefinition = "TEXT", nullable = true)
    String problemStructure;

//    List<Lesson> lessons;
//
//    Topic topic;
//
//    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
//    List<Hint> hints;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("problem-testcase")
    List<TestCase> testCases;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("problem-submissions")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//    @JsonBackReference("submissions-problem")
    List<ProblemSubmission> submissions;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("problem-solution")
    List<Solution> solutions;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    List<ProblemCategory> categories;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    List<Hint> hints;

}