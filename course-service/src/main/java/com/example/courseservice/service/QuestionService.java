package com.example.courseservice.service;


import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.QuestionMapper;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Lesson;
import com.example.courseservice.model.Question;
import com.example.courseservice.repository.ExerciseRepository;
import com.example.courseservice.repository.LessonRepository;
import com.example.courseservice.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionService {
    QuestionRepository questionRepository;
    QuestionMapper questionMapper;
    LessonRepository lessonRepository;
    ExerciseRepository exerciseRepository;
}
