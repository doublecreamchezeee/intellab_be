package com.example.courseservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "score")
    Integer score;

    @Column(columnDefinition = "DECIMAL(11,2)")
    Float price;

    @Column(name = "unit_price", columnDefinition = "VARCHAR(10)")
    String unitPrice;

    @Column(name = "average_rating")
    Double averageRating;

    @Column(name = "review_count")
    Integer reviewCount;

    @Column(name = "current_creation_step", columnDefinition = "integer default 1") // start at 1, only increase
    Integer currentCreationStep;

    @Column(name = "is_available", columnDefinition = "boolean default false")
    Boolean isAvailable;

    @Column(name = "course_image", nullable = true)
    String courseImage;

    @Column(name = "is_completed_creation", columnDefinition = "boolean default false")
    Boolean isCompletedCreation;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<Review> reviews = new ArrayList<>();

    @JoinColumn(name = "user_id")
    UUID userId;

    Integer templateCode = 1;


    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = true)
    @JoinColumn(name = "topic_id", nullable = true)
    Topic topic;

    @JsonManagedReference
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<UserCourses> enrollCourses = new ArrayList<>();

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_category",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    List<Category> categories;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_section",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    List<Section> sections;

}
