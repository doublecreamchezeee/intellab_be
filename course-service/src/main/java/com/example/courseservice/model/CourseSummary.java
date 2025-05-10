package com.example.courseservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "\"courses_summary\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourseSummary {
    @Id
    @Column(name = "course_id", nullable = false)
    UUID courseId;

    @JsonIgnore
    @OneToOne
    @MapsId
    @JoinColumn(name = "course_id", referencedColumnName = "course_id",
            foreignKey = @ForeignKey(name = "fk_c_cs"))
    Course course;

    @Column(name = "course_name")
    String courseName;

    @Column(name = "summary_content", columnDefinition = "TEXT")
    String summaryContent;
}
