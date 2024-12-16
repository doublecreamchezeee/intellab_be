package com.example.courseservice.repository;

import com.example.courseservice.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findAllByCourse_CourseId(UUID courseId);
    void deleteAllByCourse_CourseId(UUID courseId);
    Optional<Lesson> findByLessonOrder(int lessonOrder);
    Optional<Lesson> findByLessonOrderAndCourse_CourseId( int lessonOrder, UUID courseId);
}
