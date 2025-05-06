package com.example.courseservice.model;



import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "\"exercises\"")
public class Exercise {
    @Id
    @GeneratedValue
    @Column(name = "exercise_id")
    UUID exerciseId;

    @Column(name = "exercise_name")
    String exerciseName;

    @Column(columnDefinition = "TEXT")
    String description;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    //@JsonBackReference
    Lesson lesson;

    Integer questionsPerExercise;

    Integer passingQuestions;

    @JsonIgnore
    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Question> questionList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY)
    List<Assignment> assignments;

}
