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
@SqlResultSetMapping(
        name = "DetailsLessonMapping",
        classes = @ConstructorResult(
                targetClass = DetailsLessonResponse.class,
                columns = {
                        @ColumnResult(name = "lesson_id", type = UUID.class),
                        @ColumnResult(name = "content", type = String.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "lesson_order", type = Integer.class),
                        @ColumnResult(name = "lesson_name", type = String.class),
                        @ColumnResult(name = "course_id", type = UUID.class),
                        @ColumnResult(name = "exercise_id", type = UUID.class),
                        @ColumnResult(name = "learning_id", type = UUID.class),
                        @ColumnResult(name = "next_lesson_id", type = UUID.class),
                        @ColumnResult(name = "next_lesson_name", type = String.class),
                        @ColumnResult(name = "is_done_theory", type = Boolean.class),
                        @ColumnResult(name = "is_done_practice", type = Boolean.class)
                }
        )
)
@NamedNativeQuery(
        name = "Lesson.getDetailsLesson",
        query = "SELECT * FROM get_details_lesson(:lessonId, :userId)",
        resultSetMapping = "DetailsLessonMapping"
)
public class Lesson {
    @Id
    @Column(name = "lesson_id")
    @GeneratedValue
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonBackReference
    Course course;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    Exercise exercise;

    @Column(name = "problem_id", nullable = true)
    @JsonBackReference
    UUID problemId;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "problem_id")
//    Problem problem;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    @JsonBackReference
    List<LearningLesson> learningLessons;
}
