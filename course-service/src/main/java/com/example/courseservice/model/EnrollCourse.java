package com.example.courseservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class EnrollCourse implements Serializable {

    @JoinColumn(name = "user_id", nullable = false)
    UUID userUid;

    UUID course_id;


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


