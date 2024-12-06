package com.example.courseservice.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;
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

    @OneToOne(mappedBy = "exercise")
    Lesson lesson;


    @ManyToMany
    @JoinTable(
            name = "question_list",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    Set<Question> questionsList;

    @OneToMany(mappedBy = "exercise")
    List<Assignment> assignments;




}
