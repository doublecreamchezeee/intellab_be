package com.example.courseservice.repository.custom;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;

@Repository
public interface LearningLessonRepositoryCustom {
    Page<LessonProgressResponse> getLessonProgress(UUID userId, UUID courseId, Pageable pageable);
    List<LessonProgressResponse> getLessonProgress(UUID userId, UUID courseId);
    Boolean markTheoryLessonAsDone(UUID learningId, UUID exerciseId);
}
