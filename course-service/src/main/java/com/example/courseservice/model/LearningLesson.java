package com.example.courseservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"learning_lesson\"")
@SqlResultSetMapping(
    name = "LessonProgressMapping",
    classes = @ConstructorResult(
        targetClass = LessonProgressResponse.class,
        columns = {
            @ColumnResult(name = "lesson_id", type = UUID.class),
            @ColumnResult(name = "course_id", type = UUID.class),
            @ColumnResult(name = "lesson_order", type = Integer.class),
            @ColumnResult(name = "lesson_name", type = String.class),
            @ColumnResult(name = "description", type = String.class),
            @ColumnResult(name = "content", type = String.class),
            @ColumnResult(name = "problem_id", type = UUID.class),
            @ColumnResult(name = "exercise_id", type = UUID.class),
            @ColumnResult(name = "status", type = String.class),
            @ColumnResult(name = "last_accessed_date", type = Instant.class),
            @ColumnResult(name = "learning_id", type = UUID.class),
            @ColumnResult(name = "is_done_theory", type = Boolean.class),
            @ColumnResult(name = "is_done_practice", type = Boolean.class)
        }
    )
)
@NamedNativeQuery(
    name = "LearningLesson.getLessonProgressCount",
    query = "SELECT COUNT(*) FROM get_lessons_and_learning_progress(:userId, :courseId)"
)
@NamedNativeQuery(
    name = "LearningLesson.getLessonProgress",
    query = "SELECT * FROM get_lessons_and_learning_progress(:userId, :courseId)",
    resultSetMapping = "LessonProgressMapping"
)//lesson_id, course_id, lesson_order, lesson_name, description, content, problem_id, exercise_id, status, last_accessed_date
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LearningLesson {
    @Id
    @GeneratedValue
    @Column(name = "learning_id")
    UUID learningId;

    //có ràng buộc miền giá trị trong db
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @UpdateTimestamp
    @Column(name = "last_accessed_date")
    Instant lastAccessedDate;

    @Column(name = "is_done_theory")
    Boolean isDoneTheory;

    @Column(name = "is_done_practice")
    Boolean isDonePractice;

    @JoinColumn(name = "user_id", nullable = false)
    UUID userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    Lesson lesson;

    @JsonIgnore
    @OneToMany(mappedBy = "learningLesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Assignment> assignments;

}

 /*entities={
            @EntityResult(
                    entityClass=LessonProgressResponse.class, fields={
                        @FieldResult(name = "lessonId", column="lesson_id"),
                        @FieldResult(name = "courseId", column="course_id"),
                        @FieldResult(name = "lessonOrder", column="lesson_order"),
                        @FieldResult(name = "lessonName", column="lesson_name"),
                        @FieldResult(name = "description", column="description"),
                        @FieldResult(name = "content", column="content"),
                        @FieldResult(name = "problemId", column="problem_id"),
                        @FieldResult(name = "exerciseId", column="exercise_id"),
                        @FieldResult(name = "status", column="status"),
                        @FieldResult(name = "lastAccessedDate", column="last_accessed_date"),
                    }
            )
    }*/

    /*classes = @ConstructorResult(
            targetClass = LessonProgressResponse.class,
            columns = {
                    @ColumnResult(name = "lessonId", type = UUID.class),
                    @ColumnResult(name = "courseId", type = UUID.class),
                    @ColumnResult(name = "lessonOrder", type = Integer.class),
                    @ColumnResult(name = "lessonName", type = String.class),
                    @ColumnResult(name = "description", type = String.class),
                    @ColumnResult(name = "content", type = String.class),
                    @ColumnResult(name = "problemId", type = UUID.class),
                    @ColumnResult(name = "exerciseId", type = UUID.class),
                    @ColumnResult(name = "status", type = String.class),
                    @ColumnResult(name = "lastAccessedDate", type = Instant.class)
            }
    )*/