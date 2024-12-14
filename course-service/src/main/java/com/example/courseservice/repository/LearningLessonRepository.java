package com.example.courseservice.repository;

import com.example.courseservice.model.LearningLesson;
import com.example.courseservice.repository.custom.LearningLessonRepositoryCustom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearningLessonRepository extends JpaRepository<LearningLesson, UUID>, LearningLessonRepositoryCustom {
    List<LearningLesson> findByUserId(UUID userId);
    List<LearningLesson> findByLesson_LessonId(UUID lessonId);
    Optional<LearningLesson> findByUserIdAndLesson_LessonId(UUID userId, UUID lessonId);
    void deleteByUserIdAndLesson_LessonId(UUID userId, UUID lessonId);
    void deleteByLesson_LessonId(UUID lessonId);
    Optional<LearningLesson> findByLesson_LessonIdAndUserId(UUID lessonId, UUID userId);
    List<LearningLesson> findAllByUserIdAndLesson_Course_CourseId(UUID userId, UUID courseId);
    // @Query(value = "SELECT * FROM get_lessons_and_learning_progress(:studentId, :courseId)", nativeQuery = true)
    // List<LessonProgressResponse> getLessonsAndLearningProgress(
    //         @Param("studentId") UUID studentId,
    //         @Param("courseId") UUID courseId
    // );
}
