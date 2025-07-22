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
@Table(name = "\"admin_main_code\"")
public class AdminMainCode {
    @Id
    @GeneratedValue
    @Column(name = "admin_main_code_id")
    UUID adminMainCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

    @Column(name = "admin_main_code", columnDefinition = "TEXT")
    String adminMainCode;

    @Column(name = "admin_main_language_id", columnDefinition = "integer default null")
    Integer adminMainLanguageId;
}
