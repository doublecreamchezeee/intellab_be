package com.example.courseservice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"questions\"")
public class Question {
    @Id
    @Column(name = "question_id")
    @GeneratedValue
    UUID questionId;

    Integer questionOrder = 1;

    @Column(name = "question_content", columnDefinition = "TEXT")
    String questionContent;

    // enable, disable, pending
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @Column(name = "correct_answer")
    String correctAnswer;

    // S: single-choice; M: multiple-choice
    @Column(name = "question_type")
    Character questionType;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;


    @JsonManagedReference
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Option> options = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    Exercise exercise;

    @JsonIgnore
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    List<AssignmentDetail> assignmentDetails;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_category",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    List<Category> categories = new ArrayList<>();

}

//CREATE TABLE Questions (
//        Question_ID VARCHAR(255) PRIMARY KEY,
//Question_content TEXT,
//Status VARCHAR(10),
//Correct_Answer VARCHAR(10),
//Question_type VARCHAR(10),
//Created_date TIMESTAMP,
//Last_Edited_date TIMESTAMP
//);
