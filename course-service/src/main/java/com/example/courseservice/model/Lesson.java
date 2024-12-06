package com.example.courseservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"lesson\"")
public class Lesson {
    @Id
    @Column(name = "lesson_id")
    @GeneratedValue
    UUID lessonId;
    String lesson_name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    String content;

    int lessonOrder;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    Course course;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exercise_id")
    Exercise exercise;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    Problem problem;

    @OneToMany(mappedBy = "lesson")
    List<LearningLesson> learningLessons;
}
