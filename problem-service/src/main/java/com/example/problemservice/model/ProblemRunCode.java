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
@Table(name = "\"problem_run_code\"")
public class ProblemRunCode {
    @Id
    @Column(name = "run_code_id")
    @GeneratedValue
    UUID runCodeId;

    @Column(columnDefinition = "TEXT")
    String code;

    @Column(columnDefinition = "VARCHAR(50)", name = "programming_language")
    String programmingLanguage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    @JsonBackReference("problem-submissions")
    Problem problem;

    @JoinColumn(name = "user_id")
    UUID userId;

    @OneToMany(mappedBy = "runCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("runCode-output")
    List<TestCaseRunCodeOutput> testCasesRunCodeOutput;

}
