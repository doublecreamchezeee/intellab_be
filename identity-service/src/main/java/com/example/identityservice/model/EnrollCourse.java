package com.example.identityservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"enroll_course\"")
public class EnrollCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "user_id", nullable = false)
    String userUid;

    @ElementCollection
    @CollectionTable(name = "user_courses", joinColumns = @JoinColumn(name = "enroll_course_id"))
    @Column(name = "course_id")
    private List<String> courseIds;
}
