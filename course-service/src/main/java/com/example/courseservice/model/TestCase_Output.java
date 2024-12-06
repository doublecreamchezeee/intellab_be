package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"test_case_outputs\"")
public class TestCase_Output {
    @EmbeddedId
    com.example.courseservice.model.compositeKey.testCaseOutputID testCaseOutputID;

    @Column(columnDefinition = "REAL")
    Float runtime;

    @Column(columnDefinition = "TEXT")
    String submission_output;

    String result_status;


    @ManyToOne
    @MapsId("testcase_id")
    @JoinColumn(name = "testcase_id")
    TestCase testcase;

    @ManyToOne
    @MapsId("submission_id")
    @JoinColumn(name = "submission_id")
    ProblemSubmission submission;

}
