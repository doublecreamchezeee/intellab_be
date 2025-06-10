package com.example.courseservice.client;

import com.example.courseservice.dto.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AiServiceClient {
    @PutExchange(value = "/lesson/insert-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> insertLessonEmbeddingData(@RequestParam UUID lesson_id);

    @PutExchange(value = "/lesson/update-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> updateLessonEmbeddingData(@RequestParam UUID lesson_id);

    @DeleteExchange(value = "/lesson/delete-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> deleteLessonEmbeddingData(@RequestParam UUID lesson_id);

    @PutExchange(value = "/course/insert-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE) //,)
    Mono<ApiResponse<Boolean>> insertCourseEmbeddingData(@RequestParam UUID course_id);

    @PutExchange(value = "/course/update-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> updateCourseEmbeddingData(@RequestParam UUID course_id);

    @DeleteExchange(value = "/course/delete-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> deleteCourseEmbeddingData(@RequestParam UUID course_id);

    @GetExchange(value = "/info")
    Mono<Object> getAiServiceInfo();
}
