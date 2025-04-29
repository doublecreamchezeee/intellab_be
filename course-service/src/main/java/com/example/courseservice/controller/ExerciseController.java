package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.exercise.ExerciseDetailResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.service.ExerciseService;
import com.example.courseservice.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Exercise")
public class ExerciseController {
    ExerciseService exerciseService;
    QuestionService questionService;

}
