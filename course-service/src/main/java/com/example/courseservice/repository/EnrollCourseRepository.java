package com.example.courseservice.repository;

import com.example.courseservice.model.EnrollCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollCourseRepository extends JpaRepository<EnrollCourse, Long> {
    EnrollCourse findByUserUid(String userUid);
}
