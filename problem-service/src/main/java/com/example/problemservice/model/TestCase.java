package com.example.problemservice.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "\"test_cases\"")
public class TestCase {
    @Id
    @GeneratedValue
    @Column(name = "testcase_id")
    UUID testcaseId;

    @Column(columnDefinition = "TEXT")
    String input;

    @Column(columnDefinition = "TEXT")
    String output;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    @JsonBackReference("problem-testcase")
    Problem problem;

    @Column(name = "testcase_order")
    Integer order;

    // @JoinColumn(name = "user_id")
    // UUID userId;

    @OneToMany(mappedBy = "testcase", fetch = FetchType.LAZY)
    @JsonManagedReference("testcase-output")
    List<TestCaseOutput> submitOutputs;

    @OneToMany(mappedBy = "testcase", fetch = FetchType.LAZY)
    @JsonManagedReference("runCode-output")
    List<TestCaseRunCodeOutput> runCodeOutputs;
}