package com.example.courseservice.specification;

import com.example.courseservice.model.UserCourses;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserCoursesSpecification {
    public static Specification<UserCourses> hasUserUid(UUID userUid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("enrollId").get("userUid"), userUid);
    }

    public static Specification<UserCourses> hasCourseId(UUID courseId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("course").get("courseId"), courseId);
    }

    public static Specification<UserCourses> isEnrollUsingSubscription(Boolean enrollUsingSubscription) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("enrollUsingSubscription"), enrollUsingSubscription);
    }

    public static Specification<UserCourses> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<UserCourses> hasAccessStatus(String accessStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("accessStatus"), accessStatus);
    }

    public static Specification<UserCourses> hasCertificateId(UUID certificateId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("certificate").get("certificateId"), certificateId);
    }

}
