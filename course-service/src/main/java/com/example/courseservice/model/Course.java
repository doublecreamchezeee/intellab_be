package com.example.courseservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;


import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"courses\"")
public class Course {
    @Id
    @Column(name = "course_id")
    @GeneratedValue
    UUID courseId;

    @Column(name = "course_name")
    String courseName;

//    @Lob
    @Column(columnDefinition = "TEXT")
    String description;

    // beginner, intermediate, advance
    @Column(columnDefinition = "VARCHAR(20)")
    String level;

    @Column(columnDefinition = "DECIMAL(11,2)")
    Float price;

    @Column(name = "unit_price", columnDefinition = "VARCHAR(10)")
    String unitPrice;

//    @Lob
    @Column(name = "course_logo", columnDefinition = "TEXT")
    String courseLogo;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Review> reviews = new ArrayList<>();

    @JoinColumn(name = "admin_id")
    UUID userUid;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = true)
    @JoinColumn(name = "topic_id", nullable = true)
    @JsonManagedReference
    Topic topic;

    @JsonManagedReference
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<UserCourses> enrollCourses = new ArrayList<>();
}
