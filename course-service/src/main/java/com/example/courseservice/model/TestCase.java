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
@Table(name = "\"test_cases\"")
public class TestCase {
    @Id
    @GeneratedValue
    UUID testcase_id;

    @Column(columnDefinition = "TEXT")
    String input;

    @Column(columnDefinition = "TEXT")
    String output;

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    Problem problem;

    @JoinColumn(name = "source_id")
    UUID user_id;

    @OneToMany(mappedBy = "testcase")
    List<TestCase_Output> submit_outputs;

}
