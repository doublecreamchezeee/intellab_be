package com.example.courseservice.service;

import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;
    EnrollCourseRepository enrollCourseRepository;

    public List<CourseCreationResponse> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toCourseCreationResponse).toList();
    }
//    public DetailCourseResponse getCourseById(UUID id, UUID userUid) {
//        if (userUid != null) {
//            EnrollCourse enrollCourse = enrollCourseRepository.findByUserUid(userUid);
//            if (enrollCourse != null && enrollCourse.getCourseIds().contains(id)) {
//                return courseMapper.toDetailCourseResponse(courseRepository.findById(id).orElse(null), true);
//            }
//            //return null;
//        }
//        return courseMapper.toDetailCourseResponse(courseRepository.findById(id).orElse(null), false);
//    }

    public void deleteCourseById(UUID id) {
        courseRepository.deleteById(id);
    }

    public CourseCreationResponse createCourse(CourseCreationRequest request) {
        Course course = courseMapper.toCourse(request);

        course.setLessons(new ArrayList<>());
        course = courseRepository.save(course);

        return courseMapper.toCourseCreationResponse(course);
    }

    public CourseCreationResponse updateCourse(UUID courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        courseMapper.updateCourse(course, request);

        /*List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        course.setLessons(lessons);*/

        course = courseRepository.save(course);
        return courseMapper.toCourseCreationResponse(course);
    }

    public List<CourseCreationResponse> searchCourses(String keyword) {
        return courseRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword)
                .stream().map(courseMapper::toCourseCreationResponse).toList();
    }

//    public EnrollCourse enrollCourse(UUID userUid, UUID courseId) {
//        if (userUid == null || courseId == null) {
//            throw new AppException(ErrorCode.BAD_REQUEST);
//        }
//
//        EnrollCourse enrollCourse = enrollCourseRepository.findByUserUid(userUid);
//
//        // Ensure courseIds is initialized to avoid NullPointerException
//        if (enrollCourse == null) {
//            enrollCourse = new EnrollCourse();
//            enrollCourse.setUserUid(userUid);
//            enrollCourse.setCourseIds(Collections.singletonList(courseId)); // Initialize with courseId
//        } else {
//            if (enrollCourse.getCourseIds() == null) {
//                enrollCourse.setCourseIds(new ArrayList<>());  // Initialize the list if it's null
//            }
//            enrollCourse.getCourseIds().add(courseId); // Add the courseId to the list
//        }
//
//        return enrollCourseRepository.save(enrollCourse);
//    }
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
