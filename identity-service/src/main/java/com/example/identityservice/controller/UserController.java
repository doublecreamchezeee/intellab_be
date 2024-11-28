package com.example.identityservice.controller;


import com.example.identityservice.dto.response.course.CourseResponse;
import com.example.identityservice.model.EnrollCourse;
import com.example.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/course")
    public EnrollCourse enrollCourse(@RequestParam String userUid, @RequestParam String courseId) {
        return userService.enrollCourse(userUid, courseId);
    }

    @GetMapping("/courses")
    public List<CourseResponse> getUserCourses(@RequestParam String userUid) {
        return userService.getUserCourses(userUid);
    }

}
