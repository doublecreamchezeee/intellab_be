package com.example.courseservice.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;

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
public class LearningLesson {
    @Id
    @GeneratedValue
    UUID learning_id;

    Integer lesson_order;

    //có ràng buộc miền giá trị trong db
    @Column(columnDefinition = "VARCHAR(10)")
    String status;

    @UpdateTimestamp
    Instant last_accessed_date;

    @JoinColumn(name = "student_id", nullable = false)
    UUID user_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    Lesson lesson;

    @OneToMany(mappedBy = "learningLesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Assignment> assignments;

}
