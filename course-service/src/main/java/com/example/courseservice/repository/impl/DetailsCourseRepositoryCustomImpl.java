package com.example.courseservice.repository.impl;

import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.repository.custom.DetailsCourseRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class DetailsCourseRepositoryCustomImpl implements DetailsCourseRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<DetailCourseResponse> getDetailsCourse(UUID courseId, UUID userId) {
        Query query = entityManager.createNamedQuery(
                "Course.getDetailsCourse",
               // "DetailCourseMapping");
                DetailCourseResponse.class);
        query.setParameter(String.valueOf("courseId"), courseId);
        query.setParameter(String.valueOf("userId"), userId);
        return query.getResultList().stream().findFirst();
    }
}
