package com.example.courseservice.repository.impl;

import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import com.example.courseservice.repository.custom.DetailsLessonRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class DetailsLessonRepositoryCustomImpl implements DetailsLessonRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<DetailsLessonResponse> getDetailsLesson(UUID lessonId, UUID userId) {
        Query query = entityManager.createNamedQuery(
                "Lesson.getDetailsLesson",
                DetailsLessonResponse.class);
        query.setParameter(String.valueOf("lessonId"), lessonId);
        query.setParameter(String.valueOf("userId"), userId);
        return query.getResultList().stream().findFirst();
    }
}
