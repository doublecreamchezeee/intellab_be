package com.example.problemservice.client;

import com.example.problemservice.dto.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public interface AiServiceClient {
    @PutMapping(value = "/problem/insert-embedding-data", consumes =  MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> insertProblemEmbeddingData(@RequestParam UUID problemId);

    @PutMapping(value = "/problem/update-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> updateProblemEmbeddingData(@RequestParam UUID problemId);

    @DeleteMapping(value = "/problem/delete-embedding-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> deleteProblemEmbeddingData(@RequestParam UUID problemId);
}
