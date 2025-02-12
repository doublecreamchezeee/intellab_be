package com.example.problemservice.client;

import com.example.problemservice.dto.request.lesson.DonePracticeRequest;
import com.example.problemservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Component
@FeignClient(name = "course-service", url = "${COURSE_SERVICE_URL}")
public interface CourseClient {
    @PutMapping(value = "/lessons/{problemId}/{UserId}/donePracticeByProblemId", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> donePracticeByProblemId(@PathVariable UUID problemId, @PathVariable UUID UserId);
}
