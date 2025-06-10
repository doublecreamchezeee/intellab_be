package com.example.courseservice.repository;

import com.example.courseservice.model.Course;
import com.example.courseservice.model.Topic;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
    Page<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Page<Course> findAllBySections_Id(Integer sectionsId, Pageable pageable);

    List<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    List<Course> findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndLevel(String courseName, String description, String level);

    List<Course> findAllByDescriptionContainingIgnoreCaseAndCategories_Id(String description,
                                                                            Integer categoryName);

    List<Course> findAllByCourseNameContainingIgnoreCaseAndCategories_Id(String name,
                                                                           Integer categoryId);

    @Query("SELECT c FROM Course c WHERE c.isAvailable = true and c.courseId NOT IN (SELECT uc.enrollId.courseId FROM UserCourses uc WHERE uc.enrollId.userUid = :userId AND uc.accessStatus LIKE 'accessible')")
    //@EntityGraph(attributePaths = {"lessons"})
    Page<Course> findAllCoursesExceptEnrolledByUser(UUID userId, Pageable pageable);

    List<Course> findAllByCourseNameContainingIgnoreCaseAndLevel(String keyword, String level);

    List<Course> findAllByDescriptionContainingIgnoreCaseAndLevel(String description, String level);

    Course findByCourseIdAndUserId(UUID id, UUID userId);

    @NotNull
    //@EntityGraph(attributePaths = {"lessons"})
    Page<Course> findAll(Specification<Course> specification, @NotNull Pageable pageable);

    //@EntityGraph(attributePaths = {"lessons"})
    Page<Course> findByUserId(Pageable pageable, UUID userId);

    Course findByTopic(Topic topic);

    @Query("SELECT c " +
            "FROM Course c " +
            "WHERE c.isAvailable = true " +
            "   and c.courseId " +
            "IN (SELECT uc.enrollId.courseId " +
            "       FROM UserCourses uc " +
            "       WHERE uc.enrollId.userUid = :userId " +
            "       AND uc.accessStatus " +
            "       LIKE 'accessible')")
    Page<Course> findAllCoursesEnrolledByUser(UUID userId, Pageable pageable);

    @Query("SELECT count(uc.enrollId.courseId) " +
            "       FROM UserCourses uc " +
            "       WHERE uc.enrollId.userUid = :userId " +
            "       AND uc.accessStatus " +
            "       LIKE 'accessible'")
    int countAllEnrolledByUser(UUID userId);

    /*@Query("SELECT c " +
            "FROM Course c " +
            "WHERE c.isAvailable = true " +
            "   and c.courseId " +
            "NOT IN (SELECT uc.enrollId.courseId " +
            "       FROM UserCourses uc " +
            "       WHERE uc.enrollId.userUid = :userId " +
            "       AND uc.accessStatus " +
            "       LIKE 'accessible')")*/

    //@Query("SELECT ec from (SELECT uc FROM UserCourses uc) AS uc
    // RIGHT JOIN (SELECT c FROM Course c
    // WHERE c.isAvailable = true and c.price > 0 and c.courseId
    // NOT IN
    // (SELECT uc.enrollId.courseId
    // FROM UserCourses uc
    // WHERE uc.enrollId.userUid = :userId AND uc.accessStatus LIKE 'accessible'))
    // AS ec ON ec.courseId = uc.enrollId.courseId")

    /*@Query("SELECT oc " +
            "FROM " +
            "   (SELECT c FROM Course c" +
            "           WHERE c.isAvailable = true AND c.price > 0 AND c.courseId NOT IN " +
            "               (SELECT uc.enrollId.courseId FROM UserCourses uc WHERE uc.enrollId.userUid = :userId AND uc.accessStatus LIKE 'accessible')" +
            "       ) AS oc" +
            "   LEFT JOIN (SELECT ucinner FROM UserCourses ucinner WHERE ucinner.accessStatus LIKE 'accessible') AS uc ON uc.enrollId.courseId = oc.courseId" +
            " GROUP BY oc.courseId" +
            "   ORDER BY COUNT(uc.enrollId.userUid) DESC")*/

            //"  LIMIT :limit") //

    /*@Query("""
      SELECT c
      FROM Course c
        LEFT JOIN UserCourses uc
          ON uc.enrollId.courseId = c.courseId
         AND uc.enrollId.userUid = :userId
         AND uc.accessStatus = 'accessible'
      WHERE
        c.isAvailable = TRUE
        AND c.price > 0
        AND uc.enrollId.courseId IS NULL
      GROUP BY c.courseId
      ORDER BY COUNT(uc.enrollId.userUid) DESC
     """, nativeQuery=true)*/

    @Query("SELECT c " +
            "FROM Course c " +
            "LEFT JOIN UserCourses uc ON uc.enrollId.courseId = c.courseId AND uc.accessStatus = 'accessible' " +
            "WHERE c.isAvailable = true AND c.price > 0 AND c.courseId NOT IN " +
            "(SELECT ucSub.enrollId.courseId FROM UserCourses ucSub WHERE ucSub.enrollId.userUid = :userId AND ucSub.accessStatus = 'accessible') " +
            "GROUP BY c.courseId " +
            "ORDER BY COUNT(uc.enrollId.userUid) DESC" +
            "  LIMIT :limit")
    List<Course> findTopPaidCoursesByIsAvailableTrueOrderAndExcludeUserEnrolledCoursesByNumberOfEnrolledStudentsDesc(UUID userId, int limit);

    @Query("SELECT c " +
            "FROM Course c " +
            "LEFT JOIN UserCourses uc ON uc.enrollId.courseId = c.courseId AND uc.accessStatus = 'accessible' " +
            "WHERE c.isAvailable = true AND c.price > 0 " +
            "GROUP BY c.courseId " +
            "ORDER BY COUNT(uc.enrollId.userUid) DESC" +
            "  LIMIT :limit")
    List<Course> findTopPaidCoursesByIsAvailableTrueOrderByNumberOfEnrolledStudentsDesc(int limit);//Pageable pageable

    @Query("SELECT c " +
            "FROM Course c " +
            "WHERE c.isAvailable = true AND c.price = 0 ")
    Page<Course> findByIsAvailableTrueAndPriceEqualsZero(Pageable pageable);

    @Query("SELECT c " +
            "FROM Course c " +
            "WHERE c.isAvailable = true AND c.price = 0 " +
            "   and c.courseId " +
            "NOT IN (SELECT uc.enrollId.courseId " +
            "       FROM UserCourses uc " +
            "       WHERE uc.enrollId.userUid = :userId " +
            "       AND uc.accessStatus LIKE 'accessible')")
    Page<Course> findByIsAvailableTrueAndPriceEqualsZeroAndExcludeUserEnrolledCourses(UUID userId, Pageable pageable);

//    @Query("SELECT c FROM Course c" +
//            " WHERE c.isAvailable = true AND c.price > 0 AND c.courseId NOT IN " +
//            "(SELECT uc.enrollId.courseId FROM UserCourses uc WHERE uc.enrollId.userUid = :userId AND uc.accessStatus LIKE 'accessible') ")
//    List<Course> findTop10PaidCoursesByIsAvailableTrueOrderByNumberOfEnrolledStudentsDesc(Pageable pageable);
}
