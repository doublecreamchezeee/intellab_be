package com.example.courseservice.client;

import com.example.courseservice.dto.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AiServiceClient {
    @PutMapping(value = "/lesson/insert-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> insertLessonEmbeddingData(@RequestParam UUID lessonId);

    @PutMapping(value = "/lesson/update-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> updateLessonEmbeddingData(@RequestParam UUID lessonId);

    @DeleteMapping(value = "/lesson/delete-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> deleteLessonEmbeddingData(@RequestParam UUID lessonId);

    @PutMapping(value = "/course/insert-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> insertCourseEmbeddingData(@RequestParam UUID courseId);

    @PutMapping(value = "/course/update-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> updateCourseEmbeddingData(@RequestParam UUID courseId);

    @DeleteMapping(value = "/course/delete-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> deleteCourseEmbeddingData(@RequestParam UUID courseId);
}
