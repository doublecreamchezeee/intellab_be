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
@Table(name = "\"exercises\"")
public class Exercises {
    @Id
    @GeneratedValue
    UUID exercise_id;
    String exercise_name;
    @Lob
    String description;

    @OneToOne(mappedBy = "exercises")
    Lesson lesson;



}
