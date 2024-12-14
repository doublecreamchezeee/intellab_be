package com.example.courseservice.repository.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.repository.custom.LearningLessonRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;

@Repository
public class LearningLessonRepositoryCustomImpl implements LearningLessonRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LessonProgressResponse> getLessonProgress(UUID userId, UUID courseId) {
        StoredProcedureQuery query =  entityManager.createStoredProcedureQuery("LearningLesson.getLessonProgress", LessonProgressResponse.class);
        //(StoredProcedureQuery) entityManager.createNamedQuery("LearningLesson.getLessonProgress");  get_lessons_and_learning_progress
    
        query.registerStoredProcedureParameter("studentId", UUID.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("courseId", UUID.class, ParameterMode.IN);
        query.setParameter("studentId", userId);
        query.setParameter("courseId", courseId);
        System.out.println("result: " + query.getResultList());
        return query.getResultList();
    }
}