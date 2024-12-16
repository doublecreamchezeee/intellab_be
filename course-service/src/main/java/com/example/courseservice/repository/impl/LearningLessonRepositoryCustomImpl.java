package com.example.courseservice.repository.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.repository.custom.LearningLessonRepositoryCustom;

@Repository
public class LearningLessonRepositoryCustomImpl implements LearningLessonRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LessonProgressResponse> getLessonProgress(UUID userId, UUID courseId) {
        Query query = entityManager.createNamedQuery(
                "LearningLesson.getLessonProgress",
                LessonProgressResponse.class
        );
        /*StoredProcedureQuery query = entityManager.createStoredProcedureQuery(
                "get_lessons_and_learning_progress",
                "LessonProgressMapping"

                );*/
        //LessonProgressResponse.class
        /*StoredProcedureQuery query = entityManager.createNamedStoredProcedureQuery(
                "get_lessons_and_learning_progress"
        );*/

        //"LessonProgressMapping"
        // get_lessons_and_learning_progress

        //(StoredProcedureQuery) entityManager.createNamedQuery("LearningLesson.getLessonProgress");  get_lessons_and_learning_progress
        // LearningLesson.getLessonProgress
        /*Query query = entityManager.createNativeQuery(
                "SELECT * FROM get_lessons_and_learning_progress(:student_id, :course2_id)"
        );*/
        /*Query query = entityManager.createNativeQuery(
                "SELECT lesson_id, course_id, lesson_order, lesson_name, description, content, problem_id, exercise_id, status, last_accessed_date " +
                        "FROM get_lessons_and_learning_progress("
                        + "'" + userId + "', '" + courseId +  "');",
                "LessonProgressMapping"
                );*/
        //?{student_id}, ?{course2_id}
        //query.registerStoredProcedureParameter("student_id", UUID.class, ParameterMode.IN);
       // query.registerStoredProcedureParameter("course2_id", UUID.class, ParameterMode.IN);
         //LessonProgressResponse.class
        //query.setParameter("student_id", userId);
        //query.setParameter("course2_id", courseId);
        //query.setParameter("student_id", userId);
        //query.setParameter("course2_id", courseId);
         /*System.out.println("result: " + query.getResultList());
         return query.getResultList();*/
        //query.registerStoredProcedureParameter("userId", UUID.class, ParameterMode.IN);
        //query.registerStoredProcedureParameter("courseId", UUID.class, ParameterMode.IN);
        //query.setParameter("userId", userId);
        //query.setParameter("courseId", courseId);

        query.setParameter(String.valueOf("userId"), userId);
        query.setParameter(String.valueOf("courseId"), courseId);
        return query.getResultList();
       // List<Object[]> results = query.getResultList();
        //return results.stream().map(this::mapToLessonProgressResponse).collect(Collectors.toList());
    }

    /*private LessonProgressResponse mapToLessonProgressResponse(Object[] result) {
        return LessonProgressResponse.builder()
                .lesson_id((UUID) result[0])
                .course_id((UUID) result[1])
                .lesson_order((Integer) result[2])
                .lesson_name((String) result[3])
                .description((String) result[4])
                .content((String) result[5])
                .problem_id((UUID) result[6])
                .exercise_id((UUID) result[7])
                .status((String) result[8])
                .last_accessed_date((Instant) result[9])
                .learning_id((UUID) result[10])
                .build();
    }*/
}