package com.example.problemservice.core;

import com.example.problemservice.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataGenerator {
    private final ProblemService problemService;
    @EventListener(ApplicationReadyEvent.class)
    public void generateDataAfterStartup() {
        log.info("=== Application Started. Generating Data ===");
        problemService.generateBoilerplate();
    }
}
