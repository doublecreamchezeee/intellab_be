package com.example.courseservice.model.compositeKey;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.rmi.server.UID;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class EnrollCourse implements Serializable {

    UUID userUid;
    UUID courseId;
}


//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//@Entity
//@Table(name = "\"enroll_course\"")
//public class EnrollCourse {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    Long id;
//
//    @JoinColumn(name = "user_id", nullable = false)
//    UUID userUid;
//
//    @ElementCollection
//    @CollectionTable(name = "user_courses", joinColumns = @JoinColumn(name = "enroll_course_id"))
//    @Column(name = "course_id")
//    private List<UUID> courseIds;
//}


