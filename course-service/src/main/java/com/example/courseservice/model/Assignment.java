package com.example.courseservice.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"assignments\"")
public class Assignment {
    @Id
    @GeneratedValue
    UUID assignment_id;

    Integer submit_order;

    @Column(columnDefinition = "DECIMAL(4,2)")
    Float score;

    @CreationTimestamp
    Instant submit_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id",nullable = false)
    Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_id",nullable = false)
    LearningLesson learningLesson;

    @OneToMany(mappedBy = "assignment")
    List<AssignmentDetail> assignment_details;

}
