package com.example.courseservice.service;

import com.example.courseservice.dto.request.CourseCreationRequest;
import com.example.courseservice.dto.request.CourseUpdateRequest;
import com.example.courseservice.dto.response.CourseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Lesson;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.LessonRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;
    LessonRepository lessonRepository;

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toCourseResponse).toList();
    }
    public CourseResponse getCourseById(String id) {
        return courseMapper.toCourseResponse(courseRepository.findById(id).orElse(null));
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

}
