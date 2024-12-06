package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "\"problem_submissions\"")
public class ProblemSubmission {
    @Id
    @GeneratedValue
    UUID submission_id;
    Integer submit_order;

    @Column(columnDefinition = "TEXT")
    String code;

    @Column(columnDefinition = "VARCHAR(50)")
    String programming_language;
    Integer score_achieved;

    @CreationTimestamp
    Instant submit_date;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    Problem problem;

    @JoinColumn(name = "user_id")
    UUID userUid;

    @OneToMany(mappedBy = "submission")
    List<TestCase_Output> testCases_output;

}
