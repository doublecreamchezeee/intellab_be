package com.example.courseservice.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @OneToOne(mappedBy = "exercise", fetch = FetchType.LAZY)
    Lesson lesson;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_list",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    List<Question> questionList;

    @JsonIgnore
    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY)
    List<Assignment> assignments;

}
