package com.example.problemservice.client;

import com.example.problemservice.dto.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AiServiceClient {
    @PutExchange(value = "/problem/insert-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> insertProblemEmbeddingData(@RequestParam UUID problem_id);

    @PutExchange(value = "/problem/update-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> updateProblemEmbeddingData(@RequestParam UUID problem_id);

    @DeleteExchange(value = "/problem/delete-embedding-data", accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Boolean>> deleteProblemEmbeddingData(@RequestParam UUID problem_id);
}
