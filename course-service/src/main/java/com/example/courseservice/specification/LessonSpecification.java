package com.example.courseservice.specification;

import com.example.courseservice.model.Lesson;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class LessonSpecification {
    public static Specification<Lesson> hasProblemId() {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.isNotNull(root.get("problemId"));
    }

    public static Specification<Lesson> hasExerciseId() {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.isNotNull(root.get("exercise"));
    }

    public static Specification<Lesson> hasCourseId(UUID courseId) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("course").get("courseId"), courseId);
    }

    public  static Specification<Lesson> greaterThanLessonOrder(Integer lessonOrder) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.greaterThan(root.get("lessonOrder"), lessonOrder);
    }
}
