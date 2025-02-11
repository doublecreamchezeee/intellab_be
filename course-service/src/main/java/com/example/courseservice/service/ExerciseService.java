package com.example.courseservice.service;

import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.response.exercise.AddQuestionToExerciseResponse;
import com.example.courseservice.dto.response.exercise.ExerciseDetailResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.ExerciseMapper;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Question;
import com.example.courseservice.repository.ExerciseRepository;
import com.example.courseservice.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class ExerciseService {
    ExerciseRepository exerciseRepository;
    ExerciseMapper exerciseMapper;

    QuestionRepository questionRepository;

    public ExerciseResponse createExercise(ExerciseCreationRequest request)
    {
        return exerciseMapper.toExerciseResponse(exerciseRepository.save(exerciseMapper.toExercise(request)));
    }

//    public Exercise create(Exercise exercise) {};
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll().stream().map(exerciseMapper::toExerciseResponse).toList();
    }

    public ExerciseDetailResponse getExerciseById(UUID id) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        List<UUID> questionIds = exercise.getQuestionList().stream().map(Question::getQuestionId).collect(Collectors.toList());

        return new ExerciseDetailResponse(exercise,questionIds);

    }

    public AddQuestionToExerciseResponse addQuestionToExxerciseResponse(UUID exerciseId, UUID questionId) {
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow(()->new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        exercise.getQuestionList().add(question);
        exerciseRepository.save(exercise);
        return new AddQuestionToExerciseResponse(exerciseId,questionId);
    }


    public void removeQuestionFromExercise(UUID exerciseId, UUID questionId) {
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        exercise.getQuestionList().remove(question);
        exerciseRepository.save(exercise);
    }



}
