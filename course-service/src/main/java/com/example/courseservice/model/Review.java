package com.example.courseservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
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
    @Column(name = "review_id")
    UUID reviewId;

    int rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    @JoinColumn(name = "user_uuid", nullable = false)
    UUID userUuid; //  PostgreSQL UUID

    @JoinColumn(name = "user_uid", columnDefinition = "VARCHAR",nullable = false)
    String userUid; // Firebase UID

    @Column(name = "create_at")
    @CreationTimestamp
    Instant createAt;

    @Column(name = "last_modified_at")
    @UpdateTimestamp
    Instant lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    Course course;
}
