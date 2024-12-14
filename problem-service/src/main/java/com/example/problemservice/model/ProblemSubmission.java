package com.example.problemservice.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
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
    UUID submission_id;

    Integer submit_order;

    @Column(columnDefinition = "TEXT")
    String code;

    @Column(columnDefinition = "VARCHAR(50)")
    String programming_language;
    Integer score_achieved;

//    @CreationTimestamp
//    Instant submit_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    @JsonBackReference("problem-submissions")
    Problem problem;

    @JoinColumn(name = "user_id")
    UUID userUid;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("submission-output")
    List<TestCase_Output> testCases_output;

}
