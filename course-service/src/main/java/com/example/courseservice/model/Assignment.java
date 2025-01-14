package com.example.courseservice.model;



import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "exercise_id", nullable = true)
    Exercise exercise;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_id",nullable = false)
    LearningLesson learningLesson;

    @JsonManagedReference
    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    List<AssignmentDetail> assignment_details;

}
