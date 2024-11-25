package com.example.courseservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"lesson\"")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String description;
    String content;
    int lessonOrder;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
