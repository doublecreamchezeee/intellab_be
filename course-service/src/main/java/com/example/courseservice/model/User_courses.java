package com.example.courseservice.model;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"user_courses\"")
public class User_courses {
    @EmbeddedId
    EnrollCourse EnrollID;
    Float progress_percent;
    String status;

    @UpdateTimestamp
    Instant last_accessed_date;
}
