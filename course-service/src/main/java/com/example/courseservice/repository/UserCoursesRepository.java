package com.example.courseservice.repository;

import com.example.courseservice.model.UserCourses;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.rmi.server.UID;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCoursesRepository extends JpaRepository<UserCourses, EnrollCourse>, JpaSpecificationExecutor<UserCourses> {
    Optional<UserCourses> findByEnrollIdUserUid(UUID userUid);
    Optional<UserCourses> findByEnrollId_UserUidAndEnrollId_CourseId(UUID userUid, UUID courseId);
    List<UserCourses> findAllByEnrollId_CourseId(UUID courseId);
    List<UserCourses> findAllByEnrollId_UserUid(UUID userUid);
    Page<UserCourses> findAllByEnrollId_UserUid(UUID userUid, Pageable pageable);
    boolean existsByEnrollId_UserUidAndEnrollId_CourseId(UUID userUid, UUID courseId);
    boolean existsByEnrollId_UserUidAndEnrollId_CourseIdAndAccessStatus(UUID userUid, UUID courseId, String accessStatus);

    @Query("SELECT uc.enrollId.userUid, " +
            "SUM(c.score), " +
            "SUM(CASE WHEN c.level = 'Beginner' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.level = 'Intermediate' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.level = 'Advanced' THEN 1 ELSE 0 END) " +
            "FROM UserCourses uc " +
            "JOIN uc.course c " +
            "WHERE uc.status = 'Done' " +
            "GROUP BY uc.enrollId.userUid " +
            "ORDER BY SUM(c.score) DESC")
    List<Object[]> getLeaderboard();

}
