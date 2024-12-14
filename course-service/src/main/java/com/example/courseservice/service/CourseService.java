package com.example.courseservice.service;

import com.example.courseservice.constant.PredefinedLearningStatus;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.UserCourses;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.UserCoursesRepository;
import com.example.courseservice.utils.ParseUUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;
    UserCoursesRepository userCoursesRepository;

    public List<CourseCreationResponse> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toCourseCreationResponse).toList();
    }

    public void deleteCourseById(UUID id) {
        courseRepository.deleteById(id);
    }

    public CourseCreationResponse createCourse(UUID userUid, CourseCreationRequest request) {
        Course course = courseMapper.toCourse(request);

        course.setUserUid(userUid);
        course.setLessons(new ArrayList<>());
        course.setReviews(new ArrayList<>());
        course.setEnrollCourses(new ArrayList<>());

        course.setTopic(null);

        course = courseRepository.save(course);

        return courseMapper.toCourseCreationResponse(course);
    }

    public CourseCreationResponse updateCourse(UUID courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        courseMapper.updateCourse(course, request);
        course.setUserUid(ParseUUID.normalizeUID(request.getUserUid()));

        /*List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        course.setLessons(lessons);*/

        course = courseRepository.save(course);
        return courseMapper.toCourseCreationResponse(course);
    }

    public List<CourseCreationResponse> searchCourses(String keyword) {
        return courseRepository.findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream().map(courseMapper::toCourseCreationResponse).toList();
    }

    public DetailCourseResponse getCourseById(UUID courseId, UUID userUid) {
        // Check if a userUid is provided
        if (userUid != null) {
            // Find UserCourses by composite key (userUid and courseId)
            UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                    .orElse(null);

            if (userCourses == null) {
                // If user is not enrolled in the course, fetch course details without enrollment flag
                return courseMapper.toDetailCourseResponse(
                        courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED)),
                        false // User is not enrolled
                );
            }


            // If user is enrolled in the course, fetch course details
            return courseMapper.toDetailCourseResponse(
                    courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED)),
                    true // Mark that the user is enrolled
            );
        }

        // If userUid is null, fetch course details without enrollment flag
        return courseMapper.toDetailCourseResponse(
                courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED)),
                false // User is not enrolled
        );
    }

    public UserCourses enrollCourse(UUID userUid, UUID courseId) {
        if (userUid == null || courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Lấy đối tượng Course từ cơ sở dữ liệu
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Kiểm tra nếu đã có đăng ký khóa học này
        return userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                .orElseGet(() -> {
                    // Tạo đối tượng UserCourses mới
                    UserCourses newUserCourses = UserCourses.builder()
                            .enrollId(EnrollCourse.builder()
                                    .userUid(userUid)
                                    .courseId(courseId)
                                    .build())
                            .course(course)  // Đảm bảo course không null
                            .status(PredefinedLearningStatus.LEARNING)
                            .progressPercent(0.0f)
                            .build();

                    // Lưu đối tượng UserCourses mới vào cơ sở dữ liệu
                    return userCoursesRepository.save(newUserCourses);
                });
    }

    public List<UserCourses> getEnrolledUsersOfCourse(UUID courseId) {
        if (courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        return userCoursesRepository.findAllByEnrollId_CourseId(courseId);
    }

    public List<UserCourses> getEnrolledCoursesOfUser(UUID userUid) {
        if (userUid == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        return userCoursesRepository.findAllByEnrollId_UserUid(userUid);
    }
//
//    public List<CourseCreationResponse> getUserCourses(UUID userUid) {
//        EnrollCourse enrollCourse = enrollCourseRepository.findByUserUid(userUid);
//        if (enrollCourse == null || enrollCourse.getCourseIds().isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        return enrollCourse.getCourseIds().stream()
//                .map(courseId -> courseMapper.toCourseCreationResponse(
//                        courseRepository.getReferenceById(courseId)
//                ))
//                .collect(Collectors.toList());
//    }

}
