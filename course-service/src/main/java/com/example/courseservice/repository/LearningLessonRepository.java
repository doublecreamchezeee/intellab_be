package com.example.courseservice.repository;

import com.example.courseservice.model.LearningLesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningLessonRepository extends JpaRepository<LearningLesson, UUID> {
    List<LearningLesson> findByUserId(UUID userId);
    List<LearningLesson> findByLesson_LessonId(UUID lessonId);
    Optional<LearningLesson> findByUserIdAndLesson_LessonId(UUID userId, UUID lessonId);
    void deleteByUserIdAndLesson_LessonId(UUID userId, UUID lessonId);
    void deleteByLesson_LessonId(UUID lessonId);
    Optional<LearningLesson> findByLesson_LessonIdAndUserId(UUID lessonId, UUID userId);
    List<LearningLesson> findAllByUserIdAndLesson_Course_CourseId(UUID userId, UUID courseId);

    int countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDoneTheory(UUID userUid, UUID courseId, boolean isDoneTheory);
    int countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDonePractice(UUID userUid, UUID courseId, boolean isDonePractice);
    List<LearningLesson> findByUserIdAndLesson_Course_CourseIdAndStatus(UUID userId, UUID courseId, String aNew);

    // @Query(value = "SELECT * FROM get_lessons_and_learning_progress(:studentId, :courseId)", nativeQuery = true)
    // List<LessonProgressResponse> getLessonsAndLearningProgress(
    //         @Param("studentId") UUID studentId,
    //         @Param("courseId") UUID courseId
    // );
}
