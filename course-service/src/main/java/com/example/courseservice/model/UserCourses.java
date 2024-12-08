package com.example.courseservice.model;


import com.example.courseservice.model.compositeKey.EnrollCourse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


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
    @MapsId("courseId")
    @JoinColumn(name = "course_id",nullable = false)
    @JsonBackReference
    Course course;

    @Column(name = "progress_percent", columnDefinition = "DECIMAL(5,2)")
    Float progress_percent;

    // Done, Learning, Expired
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @UpdateTimestamp
    Instant last_accessed_date;
}
