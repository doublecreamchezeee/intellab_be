package com.example.identityservice.client;

import com.example.identityservice.dto.response.profile.ProgressLanguageResponse;
import com.example.identityservice.dto.response.profile.ProgressLevelResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProblemClient {
    @GetExchange("/statistics/progress/level")
    Mono<ProgressLevelResponse> getProgressLevel();

    @GetExchange("/statistics/progress/language")
    Mono<ProgressLanguageResponse> getProgressLanguage();
}
