package com.example.courseservice.repository.custom;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;

@Repository
public interface LearningLessonRepositoryCustom {
    List<Tuple> getLessonProgress(UUID userId, UUID courseId);
}
