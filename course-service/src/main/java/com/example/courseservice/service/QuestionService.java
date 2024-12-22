package com.example.courseservice.service;


import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.QuestionMapper;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Question;
import com.example.courseservice.repository.ExerciseRepository;
import com.example.courseservice.repository.LessonRepository;
import com.example.courseservice.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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


    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll().stream().map(questionMapper::toQuestionResponse).toList();
    }

    public QuestionResponse getQuestionById(UUID id) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        return questionMapper.toQuestionResponse(question);
    }

    public QuestionResponse createQuestion(QuestionCreationRequest request) {
        Question question = questionMapper.toQuestion(request);
        question = questionRepository.save(question);
        return questionMapper.toQuestionResponse(question);
    }

    public QuestionResponse updateQuestion(UUID questionId, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        questionMapper.updateQuestion(question, request);

        question = questionRepository.save(question);
        return questionMapper.toQuestionResponse(question);
    }

    public void deleteQuestion(UUID questionId) {
        questionRepository.deleteById(questionId);
    }

    public List<QuestionResponse> getQuestionsByExerciseId(UUID exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
        List<Exercise> exercises = List.of(exercise);
        //List<Question> questions  //questionMapper.toQuestionResponse(questions);
        return questionRepository.findAllByExercises(exercises)
                .stream().map(questionMapper::toQuestionResponse).toList();

    }

}
