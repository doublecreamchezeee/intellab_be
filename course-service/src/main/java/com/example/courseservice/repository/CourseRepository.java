package com.example.courseservice.repository;

import com.example.courseservice.model.Course;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Page<Course> findAllBySections_Id(Integer sectionsId, Pageable pageable);

    List<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    List<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndLevel(String courseName, String description, String level);

    List<Course> findAllByDescriptionContainingIgnoreCaseAndCategories_Id(String description,
                                                                            Integer categoryName);

    List<Course> findAllByCourseNameContainingIgnoreCaseAndCategories_Id(String name,
                                                                           Integer categoryId);

    @Query("SELECT c FROM Course c WHERE c.courseId NOT IN (SELECT uc.enrollId.courseId FROM UserCourses uc WHERE uc.enrollId.userUid = :userId)")
    Page<Course> findAllCoursesExceptEnrolledByUser(UUID userId, Pageable pageable);

    List<Course> findAllByCourseNameContainingIgnoreCaseAndLevel(String keyword, String level);

    List<Course> findAllByDescriptionContainingIgnoreCaseAndLevel(String description, String level);

    Course findByCourseIdAndUserId(UUID id, UUID userId);

    @NotNull
    Page<Course> findAll(Specification<Course> specification, @NotNull Pageable pageable);
}
