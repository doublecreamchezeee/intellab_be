package com.example.courseservice.repository.custom;

import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetailsLessonRepositoryCustom {
    Optional<DetailsLessonResponse> getDetailsLesson(UUID lessonId, UUID userId);
}
