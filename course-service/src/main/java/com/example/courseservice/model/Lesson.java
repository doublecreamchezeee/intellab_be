package com.example.courseservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"lessons\"")
public class Lesson {
    @Id
    @Column(name = "lesson_id")
    @GeneratedValue
    UUID lessonId;
    String lesson_name;

    @Column(columnDefinition = "TEXT")
    String description;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    String content;

    int lessonOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    Course course;


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    Problem problem;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    List<LearningLesson> learningLessons;
}
