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
@Table(name = "\"lesson\"")
public class Lesson {
    @Id
    @GeneratedValue
    UUID lesson_id;
    String lesson_name;
    String description;
    String content;
    int lessonOrder;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    Course course;


    @OneToOne
    @JoinColumn(name = "exercise_id")
    Exercises exercise;
}
