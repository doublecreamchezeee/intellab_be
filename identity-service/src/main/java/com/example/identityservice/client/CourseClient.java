package com.example.identityservice.client;


import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.course.DisenrollCourseRequest;
import com.example.identityservice.dto.request.course.DisenrollCoursesEnrolledUsingSubscriptionPlanRequest;
import com.example.identityservice.dto.request.course.EnrollCourseRequest;
import com.example.identityservice.dto.request.course.ReEnrollCoursesUsingSubscriptionPlanRequest;
import com.example.identityservice.dto.response.LeaderboardCourseResponse;
import com.example.identityservice.dto.response.course.CompleteCourseResponse;
import com.example.identityservice.dto.response.course.DetailCourseResponse;
import com.example.identityservice.dto.response.userCourse.UserCoursesResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CourseClient {
    /*@GetMapping(value = "/courses/{courseId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<DetailCourseResponse> getDetailCourseById(@PathVariable UUID courseId);

    @PostMapping(value = "/courses/enrollPaidCourse", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserCoursesResponse> enrollPaidCourse(@RequestBody EnrollCourseRequest request);

    @PostMapping(value = "/courses/disenroll", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> disenrollCourse(@RequestBody DisenrollCourseRequest request);*/

    @GetExchange(url = "/courses/{courseId}")
    Mono<ApiResponse<DetailCourseResponse>> getDetailCourseById(@PathVariable UUID courseId);

    @PostExchange(url = "/courses/enrollPaidCourse")
    Mono<ApiResponse<UserCoursesResponse>> enrollPaidCourse(@RequestBody EnrollCourseRequest request);

    @PostExchange(url = "/courses/disenroll")
    Mono<ApiResponse<Boolean>> disenrollCourse(@RequestBody DisenrollCourseRequest request);

    @GetExchange("/statistics/leaderboard")
    Mono<List<LeaderboardCourseResponse>> getLeaderboard();

    @GetExchange("/courses/courseList/me")
    Mono<ApiResponse<List<CompleteCourseResponse>>> getCourseByUserId();

    @PostExchange(url = "/courses/disenroll-courses-enrolled-using-subscription-plan")
    Mono<ApiResponse<Boolean>> disenrollCoursesEnrolledUsingSubscriptionPlan(@RequestBody DisenrollCoursesEnrolledUsingSubscriptionPlanRequest request);

    @PostExchange(url = "/courses/re-enroll-courses-enrolled-using-subscription-plan")
    Mono<ApiResponse<Boolean>> reEnrollCoursesEnrolledUsingSubscriptionPlan(@RequestBody ReEnrollCoursesUsingSubscriptionPlanRequest request);
}
