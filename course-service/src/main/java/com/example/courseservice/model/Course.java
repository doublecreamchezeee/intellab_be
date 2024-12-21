package com.example.courseservice.model;

import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;


import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"courses\"")
@SqlResultSetMapping(
        name = "DetailCourseMapping",
        classes = @ConstructorResult(
                targetClass = DetailCourseResponse.class,
                columns = {
                        @ColumnResult(name = "course_id", type = UUID.class),
                        @ColumnResult(name = "course_logo", type = String.class),
                        @ColumnResult(name = "course_name", type = String.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "level", type = String.class),
                        @ColumnResult(name = "price", type = Float.class),
                        @ColumnResult(name = "unit_price", type = String.class),
                        @ColumnResult(name = "user_uid", type = UUID.class),
                        @ColumnResult(name = "lesson_count", type = Integer.class),
                        @ColumnResult(name = "average_rating", type = Float.class),
                        @ColumnResult(name = "review_count", type = Integer.class),
                        @ColumnResult(name = "is_user_enrolled", type = Boolean.class),
                        @ColumnResult(name = "latest_lesson_id", type = UUID.class),
                        @ColumnResult(name = "progress_percent", type = Float.class)
                }
        )
)
@NamedNativeQuery(
        name = "Course.getDetailsCourse",
        query = "SELECT * FROM get_details_course(:courseId, :userId)",
        resultSetMapping = "DetailCourseMapping"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {
    @Id
    @Column(name = "course_id")
    @GeneratedValue
    UUID courseId;

    @Column(name = "course_name")
    String courseName;

//    @Lob
    @Column(columnDefinition = "TEXT")
    String description;

    // beginner, intermediate, advance
    @Column(columnDefinition = "VARCHAR(20)")
    String level;

    @Column(columnDefinition = "DECIMAL(11,2)")
    Float price;

    @Column(name = "unit_price", columnDefinition = "VARCHAR(10)")
    String unitPrice;

//    @Lob
    @Column(name = "course_logo", columnDefinition = "TEXT")
    String courseLogo;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<Review> reviews = new ArrayList<>();

    @JoinColumn(name = "admin_id")
    UUID userUid;


    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = true)
    @JoinColumn(name = "topic_id", nullable = true)
    @JsonManagedReference
    Topic topic;

    @JsonManagedReference
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<UserCourses> enrollCourses = new ArrayList<>();
}
