package com.example.problemservice.model;

import com.example.problemservice.model.course.Lesson;
import com.example.problemservice.model.course.Topic;
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
    UUID problem_id;

    String problem_name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "VARCHAR(20)")
    String problem_level;

    @Column(columnDefinition = "VARCHAR(50)")
    String category;
    Integer score;

    @Column(columnDefinition = "DECIMAL(5,2)")
    Float acceptance_rate;

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
//    @JsonBackReference("submissions-problem")
    List<ProblemSubmission> submissions;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("problem-solution")
    List<Solution> solutions;

}