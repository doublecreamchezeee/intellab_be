package com.example.problemservice.controller;

import com.example.problemservice.service.Judge0Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/judge0")
public class Judge0Controller {
    private final Judge0Service judge0Service;

    public Judge0Controller(Judge0Service judge0Service) {
        this.judge0Service = judge0Service;
    }

    @GetMapping("/replicas")
    public int getReplicas() {
        return judge0Service.getReplicas();
    }

    @PostMapping("/scale")
    public String scale(@RequestParam int replicas) {
        judge0Service.scaleReplicas(replicas);
        return "Scaled to " + replicas + " replicas";
    }

    @GetMapping("/pods")
    public List<String> getPods() {
        return judge0Service.getPods();
    }

    @GetMapping("/nodes")
    public List<String> getNodes() {
        return judge0Service.getNodes();
    }
}
