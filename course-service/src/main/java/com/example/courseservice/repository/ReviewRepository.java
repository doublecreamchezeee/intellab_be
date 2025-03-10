package com.example.courseservice.repository;

import com.example.courseservice.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {
    Page<Review> findAllByCourse_CourseId(UUID courseId, Pageable pageable);
    List<Review> findAllByCourse_CourseId(UUID courseId);
    Optional<Review> findByUserUidAndCourse_CourseId(String userUid, UUID course_courseId);
    Boolean existsByUserUidAndCourse_CourseId(String userUid, UUID course_courseId);
}
