package com.example.courseservice.repository;

import com.example.courseservice.model.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Page<Lesson> findAllByCourse_CourseIdOrderByLessonOrder(UUID courseId, Pageable pageable);
    List<Lesson> findAllByCourse_CourseIdOrderByLessonOrderDesc(UUID courseId);
    void deleteAllByCourse_CourseId(UUID courseId);
    Optional<Lesson> findByLessonOrder(int lessonOrder);
    Optional<Lesson> findByLessonOrderAndCourse_CourseId( int lessonOrder, UUID courseId);
    int countByCourse_CourseId(UUID courseId);
}
