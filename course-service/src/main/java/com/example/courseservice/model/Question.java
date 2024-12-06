package com.example.courseservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.Instant;
import java.util.List;
import java.util.Set;
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
    @GeneratedValue
    UUID question_id;

    @Column(name = "question_content", columnDefinition = "TEXT")
    String questionContent;

    // enable, disable, pending
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    String correct_answer;

    // S: single-choice; M: multiple-choice
    Character question_type;

    @CreationTimestamp
    Instant created_at;

    @UpdateTimestamp
    Instant updated_at;

    @OneToMany(mappedBy = "question")
    Set<Option> options;

    @ManyToMany(mappedBy = "questionsList")
    Set<Exercise> exercises;

    @OneToMany(mappedBy = "question")
    List<AssignmentDetail> assignmentDetails;

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
