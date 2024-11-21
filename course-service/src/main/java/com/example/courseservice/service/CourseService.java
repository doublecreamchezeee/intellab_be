package com.example.courseservice.service;

import com.example.courseservice.dto.response.CourseResponse;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.model.Course;
import com.example.courseservice.repository.CourseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toCourseResponse).toList();
    }
    public CourseResponse getCourseById(String id) {
        return courseMapper.toCourseResponse(courseRepository.findById(id).orElse(null));
    }
    public void deleteCourseById(String id) {
        courseRepository.deleteById(id);
    }
}
