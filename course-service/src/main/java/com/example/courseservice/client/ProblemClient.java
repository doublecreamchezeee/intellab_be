package com.example.courseservice.client;

import com.example.courseservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProblemClient {
    @GetExchange("/problem-submissions/details/{problemId}/{userId}")
    Mono<DetailsProblemSubmissionResponse> getSubmissionDetailsByProblemIdAndUserUid(
            @PathVariable("problemId") UUID problemId,
            @PathVariable("userId") UUID userUid
    );
}
