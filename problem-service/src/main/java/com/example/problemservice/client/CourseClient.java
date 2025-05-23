package com.example.problemservice.client;

import com.example.problemservice.dto.request.course.CheckingUserCourseExistedRequest;
import com.example.problemservice.dto.request.lesson.DonePracticeRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.Problem.CategoryResponse;
import com.example.problemservice.model.course.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Component
@FeignClient(name = "course-service", url = "${COURSE_SERVICE_URL}")
public interface CourseClient {
    @PutMapping(value = "/lessons/{problemId}/{UserId}/donePracticeByProblemId", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> donePracticeByProblemId(@PathVariable UUID problemId, @PathVariable UUID UserId);

    @GetMapping(value = "/courses/categories", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<Category>> categories();

    @PostMapping(value = "/courses/category", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<Category>> categories(@RequestBody List<Integer> categoryIds);

    @GetMapping(value = "/courses/category/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<CategoryResponse> categories(@PathVariable Integer categoryId);

    @PostMapping(value = "/courses/check-enrolled", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> checkEnrolled(@RequestBody CheckingUserCourseExistedRequest request);
}
