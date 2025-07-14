package com.example.problemservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"custom_checker_code\"")
public class CustomCheckerCode {
    @Id
    @GeneratedValue
    @Column(name = "custom_checker_code_id")
    UUID customCheckerCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

    @Column(name = "custom_checker_code", columnDefinition = "TEXT")
    String customCheckerCode;

    @Column(name = "custom_checker_language_id", columnDefinition = "integer default null")
    Integer customCheckerLanguageId;
}
