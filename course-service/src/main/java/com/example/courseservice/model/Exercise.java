package com.example.courseservice.model;



import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    UUID exercise_id;
    String exercise_name;

    @Column(columnDefinition = "TEXT")
    String description;

    @JsonBackReference
    @OneToOne(mappedBy = "exercise", fetch = FetchType.LAZY)
    Lesson lesson;


    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_list",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    List<Question> questionList;

    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY)
    List<Assignment> assignments;

}
