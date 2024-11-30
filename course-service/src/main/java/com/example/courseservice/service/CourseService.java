package com.example.courseservice.service;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.response.course.CourseResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.EnrollCourse;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.EnrollCourseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;
    EnrollCourseRepository enrollCourseRepository;

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toCourseResponse).toList();
    }
    public DetailCourseResponse getCourseById(String id, String userUid) {
        if (userUid != null) {
            EnrollCourse enrollCourse = enrollCourseRepository.findByUserUid(userUid);
            if (enrollCourse != null && enrollCourse.getCourseIds().contains(id)) {
                return courseMapper.toDetailCourseResponse(courseRepository.findById(id).orElse(null), true);
            }
            //return null;
        }
        return courseMapper.toDetailCourseResponse(courseRepository.findById(id).orElse(null), false);
    }

    public void deleteCourseById(String id) {
        courseRepository.deleteById(id);
    }

    public CourseResponse createCourse(CourseCreationRequest request) {
        Course course = courseMapper.toCourse(request);

        course.setLessons(new ArrayList<>());
        course = courseRepository.save(course);

        return courseMapper.toCourseResponse(course);
    }

    public CourseResponse updateCourse(String courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        courseMapper.updateCourse(course, request);

        /*List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        course.setLessons(lessons);*/

        course = courseRepository.save(course);
        return courseMapper.toCourseResponse(course);
    }

    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream().map(courseMapper::toCourseResponse).toList();
    }

    public EnrollCourse enrollCourse(String userUid, String courseId) {
        if (userUid == null || courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        EnrollCourse enrollCourse = enrollCourseRepository.findByUserUid(userUid);

        // Ensure courseIds is initialized to avoid NullPointerException
        if (enrollCourse == null) {
            enrollCourse = new EnrollCourse();
            enrollCourse.setUserUid(userUid);
            enrollCourse.setCourseIds(Collections.singletonList(courseId)); // Initialize with courseId
        } else {
            if (enrollCourse.getCourseIds() == null) {
                enrollCourse.setCourseIds(new ArrayList<>());  // Initialize the list if it's null
            }
            enrollCourse.getCourseIds().add(courseId); // Add the courseId to the list
        }

        return enrollCourseRepository.save(enrollCourse);
    }

    public List<CourseResponse> getUserCourses(String userUid) {
        EnrollCourse enrollCourse = enrollCourseRepository.findByUserUid(userUid);
        if (enrollCourse == null || enrollCourse.getCourseIds().isEmpty()) {
            return Collections.emptyList();
        }

        return enrollCourse.getCourseIds().stream()
                .map(courseId -> {
                    CourseResponse courseResponse = courseMapper.toCourseResponse(
                            courseRepository.getReferenceById(courseId)
                    );

                    return courseResponse;
                })
                .collect(Collectors.toList());
    }

}
