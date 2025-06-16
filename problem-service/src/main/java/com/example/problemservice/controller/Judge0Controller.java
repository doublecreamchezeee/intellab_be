package com.example.problemservice.controller;

import com.example.problemservice.service.Judge0Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/judge0")
public class Judge0Controller {
    private final Judge0Service judge0Service;

    public Judge0Controller(Judge0Service judge0Service) {
        this.judge0Service = judge0Service;
    }
    
    @PostMapping("/scale")
    public String scale(@RequestParam int replicas) {
        judge0Service.scaleJudge0Workers(replicas);
        return "Scaled to " + replicas + " replicas";
    }

    @GetMapping("/pods")
    public List<Map<String, Object>> getPods() {
        return judge0Service.getSimplifiedPodMetrics();
    }

    @GetMapping("/submission")
    public Long getSubmission() {
        return judge0Service.getSubmissionInQueue();
    }

    // @GetMapping("/nodes")
    // public List<String> getNodes() {
    //     return judge0Service.getNodes();
    // }
}
