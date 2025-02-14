package com.example.courseservice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"Certificates\"")
public class Certificate {
    @Id
    @GeneratedValue
    @Column(name = "certificate_id")
    UUID certificateId;

    @Column(name = "certificate_url", columnDefinition = "TEXT")
    String certificateUrl;

    @Column(name = "completed_date")
    Instant completedDate;

    @JsonIgnore
    @OneToOne(mappedBy = "certificate")
    UserCourses userCourses;
}
