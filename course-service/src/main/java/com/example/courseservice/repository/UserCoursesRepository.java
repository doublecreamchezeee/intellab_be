package com.example.courseservice.repository;

import com.example.courseservice.model.UserCourses;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.rmi.server.UID;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCoursesRepository extends JpaRepository<UserCourses, EnrollCourse> {
    Optional<UserCourses> findByEnrollIdUserUid(UID userUid);
    Optional<UserCourses> findByEnrollId_UserUidAndEnrollId_CourseId(UUID userUid, UUID courseId);
    List<UserCourses> findAllByEnrollId_CourseId(UUID courseId);
    List<UserCourses> findAllByEnrollId_UserUid(UUID userUid);
    Page<UserCourses> findAllByEnrollId_UserUid(UUID userUid, Pageable pageable);
}
