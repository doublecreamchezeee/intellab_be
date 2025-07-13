package com.example.problemservice.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.DatabindException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @Column(name = "submission_id")
    @GeneratedValue
    UUID submissionId;

    @Column(name = "submit_order")
    Integer submitOrder;

    @Column(columnDefinition = "TEXT")
    String code;

    @Column(columnDefinition = "VARCHAR(50)", name = "programming_language")
    String programmingLanguage;

    @Column(name = "score_achieved")
    Integer scoreAchieved;

    @Column(name = "is_solved")
    Boolean isSolved;

    @Column(name = "moss_report_url")
    String mossReportUrl;

    @Column(name = "is_checked_moss")
    Boolean isCheckedMoss;
//    @CreationTimestamp
//    Instant submit_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    @JsonBackReference("problem-submissions")
    Problem problem;

    @JoinColumn(name = "user_id")
    UUID userId;

    @Column(name = "is_notify")
    Boolean isNotify;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("submission-output")
    List<TestCaseOutput> testCasesOutput;

    Date createdAt;

}
