package com.example.courseservice.model;

import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"lessons\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Lesson {
    @Id
    @Column(name = "lesson_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID lessonId;

    @Column(name = "lesson_name")
    String lessonName;

    @Column(columnDefinition = "TEXT")
    String description;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    String content;

    @Column(name = "lesson_order")
    int lessonOrder;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    Course course;

    @JsonIgnore
    //@JsonManagedReference
    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Exercise exercise;

    @Column(name = "problem_id", nullable = true)
    UUID problemId;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "problem_id")
//    Problem problem;

    @JsonIgnore
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<LearningLesson> learningLessons;
}
