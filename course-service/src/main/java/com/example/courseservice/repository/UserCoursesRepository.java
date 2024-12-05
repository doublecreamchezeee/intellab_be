package com.example.courseservice.repository;

import com.example.courseservice.model.UserCourses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCoursesRepository extends JpaRepository<UserCourses, Long> {
    UserCourses findByEnrollIdUserUid(UUID userUid);
}
