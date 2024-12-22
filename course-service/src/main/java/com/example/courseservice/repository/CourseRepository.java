package com.example.courseservice.repository;

import com.example.courseservice.model.Course;
import com.example.courseservice.repository.custom.DetailsCourseRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, DetailsCourseRepositoryCustom {
    Page<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.courseId NOT IN (SELECT uc.enrollId.courseId FROM UserCourses uc WHERE uc.enrollId.userUid = :userId)")
    Page<Course> findAllCoursesExceptEnrolledByUser(UUID userId, Pageable pageable);



}
