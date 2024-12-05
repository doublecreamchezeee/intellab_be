package com.example.courseservice.repository;

import com.example.courseservice.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findAllByCourseCourseId(UUID courseId);
    void deleteAllByCourseCourseId(UUID courseId);
}
