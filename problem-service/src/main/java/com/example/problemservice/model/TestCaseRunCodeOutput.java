package com.example.problemservice.model;

import com.example.problemservice.model.composite.TestCaseRunCodeOutputId;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "\"test_case_run_code_outputs\"")
public class TestCaseRunCodeOutput {
    /*@Id
    @Column(name = "run_code_id")
    UUID runCodeId;*/

    @EmbeddedId
    TestCaseRunCodeOutputId testCaseRunCodeOutputID;

    UUID token;

    @Column(columnDefinition = "REAL")
    Float runtime;

    @Column(name = "submission_output",columnDefinition = "TEXT")
    String submissionOutput;

    @Column(name = "result_status", columnDefinition = "VARCHAR(30)")
    String resultStatus;

    @Column(name = "status_id", columnDefinition = "INTEGER", nullable = true)
    Integer statusId;

    @Column(name = "error", columnDefinition = "TEXT", nullable = true)
    String error;

    @Column(name = "message", columnDefinition = "TEXT", nullable = true)
    String message;

    @Column(name = "memory_usage", columnDefinition = "TEXT", nullable = true)
    String memoryUsage;

    @Column(name = "compile_output", columnDefinition = "TEXT", nullable = true)
    String compileOutput;

    @ManyToOne
    @MapsId("testcaseId")
    @JoinColumn(name = "testcase_id")
    @JsonBackReference("runCode-output")
    TestCase testcase;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("runCodeId")
    @JoinColumn(name = "run_code_id")
    @JsonBackReference("runCode-output")
    ProblemRunCode runCode;

    public void setRunCode(ProblemRunCode runCode) {
        this.runCode = runCode;
        if (this.testCaseRunCodeOutputID == null) {
            this.testCaseRunCodeOutputID = new TestCaseRunCodeOutputId();
        }
        this.testCaseRunCodeOutputID.setRunCodeId(runCode.getRunCodeId());
    }

    public void setTestcase(TestCase testcase) {
        this.testcase = testcase;
        if (this.testCaseRunCodeOutputID == null) {
            this.testCaseRunCodeOutputID = new TestCaseRunCodeOutputId();
        }
        this.testCaseRunCodeOutputID.setTestcaseId(testcase.getTestcaseId());
    }

    /*@Column(columnDefinition = "text[]")
    String[] output;*/

    /*@Column(name = "is_correct")
    Boolean isCorrect;

    @Column(name = "time_taken")
    Integer timeTaken;

    @Column(name = "memory_taken")
    Integer memoryTaken;*/
}
