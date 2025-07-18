package com.example.problemservice.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Date;
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

    @Column(name = "is_available", columnDefinition = "boolean default false")
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

    @OneToOne(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("problem-solution")
    Solution solution;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    List<ProblemCategory> categories;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Hint> hints;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<DefaultCode> defaultCodes;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<ProblemComment> comments;

    @Column(name = "current_creation_step", columnDefinition = "integer default 1") // start at 1, only increase
    Integer currentCreationStep;

    @Column(name = "current_creation_step_description")
    String currentCreationStepDescription;

    @Column(name = "is_completed_creation", columnDefinition = "boolean default false")
    Boolean isCompletedCreation;

    @Column(name = "author_id")
    UUID authorId;

    @Column(name = "created_at")
    Date createdAt;

    @Column(name = "has_custom_checker", columnDefinition = "boolean default false")
    Boolean hasCustomChecker;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<CustomCheckerCode> customCheckerCodes;

    @Column(name = "additional_checker_fields", columnDefinition = "TEXT")
    String additionalCheckerFields;

    @Column(name = "hidden_input_fields", columnDefinition = "TEXT")
    String hiddenInputFields;
}