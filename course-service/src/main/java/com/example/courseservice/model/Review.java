package com.example.courseservice.model;

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
@Table(name = "\"reviews\"")
public class Review {
    @Id
    @GeneratedValue
    UUID review_id;

    int rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    @JoinColumn(name = "user_id", nullable = false)
    String userUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    Course course;
}
