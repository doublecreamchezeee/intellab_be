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
@Table(name = "\"review\"")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    int rating;
    String comment;

    @JoinColumn(name = "user_id", nullable = false)
    String userUid;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    Course course;
}
