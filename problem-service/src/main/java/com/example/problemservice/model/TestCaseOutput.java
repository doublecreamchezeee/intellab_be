package com.example.problemservice.model;

import org.hibernate.annotations.CreationTimestamp;
import com.example.problemservice.model.composite.TestCaseOutputID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"test_case_outputs\"")
public class TestCaseOutput {
    @EmbeddedId
    TestCaseOutputID testCaseOutputID;

    UUID token;

    @Column(columnDefinition = "REAL")
    Float runtime;

    @Column(columnDefinition = "REAL")
    Float memory;

    @Column(columnDefinition = "TEXT")
    String submission_output;

    @Column(columnDefinition = "VARCHAR(30)")
    String result_status;

    @ManyToOne
    @MapsId("testcaseId")
    @JoinColumn(name = "testcase_id")
    @JsonBackReference("testcase-output")
    TestCase testcase;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("submissionId")
    @JoinColumn(name = "submission_id")
    @JsonBackReference("submission-output")
    ProblemSubmission submission;
    
    @CreationTimestamp
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt;

    @Column(name = "has_custom_checker", columnDefinition = "BOOLEAN DEFAULT FALSE")
    Boolean hasCustomChecker;

    @Column(name = "is_passed_by_checking_custom_checker", columnDefinition = "boolean", nullable = true)
    Boolean isPassedByCheckingCustomChecker;

    // Helper method to initialize composite ID
    public void setSubmission(ProblemSubmission submission) {
        this.submission = submission;
        if (this.testCaseOutputID == null) {
            this.testCaseOutputID = new TestCaseOutputID();
        }
        this.testCaseOutputID.setSubmissionId(submission.getSubmissionId());
    }

    public void setTestcase(TestCase testcase) {
        this.testcase = testcase;
        if (this.testCaseOutputID == null) {
            this.testCaseOutputID = new TestCaseOutputID();
        }
        this.testCaseOutputID.setTestcaseId(testcase.getTestcaseId());
    }
}


