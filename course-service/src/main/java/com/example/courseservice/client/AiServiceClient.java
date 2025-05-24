package com.example.courseservice.client;

import com.example.courseservice.dto.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AiServiceClient {
    @PutMapping(value = "/insert-lesson-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> insertLessonEmbeddingData(@RequestParam UUID lessonId);

    @PutMapping(value = "/update-lesson-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> updateLessonEmbeddingData(@RequestParam UUID lessonId);
}
