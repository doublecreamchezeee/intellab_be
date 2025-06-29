package com.example.problemservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"moss_report\"")
public class MossReport {
    @Id
    @GeneratedValue
    @Column(name = "moss_id")
    UUID mossId;


    // Khóa ngoại đến submission gốc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", referencedColumnName = "submission_id")
    @JsonBackReference("moss-submission")
    ProblemSubmission submission;

    // Khóa ngoại đến submission bị match
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_submission_id", referencedColumnName = "submission_id")
    @JsonBackReference("moss-match-submission")
    ProblemSubmission matchSubmission;

    @Column(name = "similarity_percentage")
    Double similarityPercentage;

    @Column(name = "match_lines")
    Integer matchLines;

    @Column(name = "total_lines")
    Integer totalLines;

    @Column(columnDefinition = "TEXT")
    String code;

    @Column(name = "user_id")
    UUID userId;

    @Column(name = "created_at")
    Date createdAt;
}
