package com.example.identityservice.repository;

import com.example.identityservice.model.EnrollCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollCourseRepository extends JpaRepository<EnrollCourse, Long> {
    EnrollCourse findByUserUid(String userUid);
}
