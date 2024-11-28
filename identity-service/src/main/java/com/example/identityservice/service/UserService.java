package com.example.identityservice.service;

import com.example.identityservice.client.CourseClient;
import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.response.course.CourseResponse;
import com.example.identityservice.model.EnrollCourse;
import com.example.identityservice.repository.EnrollCourseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UserService {

    EnrollCourseRepository enrollCourseRepository;
    CourseClient courseClient;

    public EnrollCourse enrollCourse(String userUid, String courseId) {
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
                    ApiResponse<CourseResponse> apiResponse = courseClient.getCourseById(courseId);
                    return apiResponse != null ? apiResponse.getResult() : null;
                })
                .collect(Collectors.toList());
    }
}