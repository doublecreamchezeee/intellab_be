package com.example.courseservice.model;


import com.example.courseservice.model.compositeKey.EnrollCourse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"user_courses\"")
public class UserCourses {
    @EmbeddedId
    EnrollCourse enrollId;


    @ManyToOne
    @JsonIgnore
    @MapsId("courseId")
    @JoinColumn(name = "course_id",nullable = false)
    Course course;

    @Column(name = "progress_percent", columnDefinition = "DECIMAL(5,2)")
    Float progressPercent;

    // Done, Learning, Expired
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @UpdateTimestamp
    @Column(name = "last_accessed_date")
    Instant lastAccessedDate;

    @Column(name = "latest_lesson_id", nullable = true)
    UUID latestLessonId;


    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    Certificate certificate;

}
