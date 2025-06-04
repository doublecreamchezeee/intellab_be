package com.example.courseservice.service;

import com.example.courseservice.dto.request.Option.OptionRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.request.exercise.ModifyQuizRequest;
import com.example.courseservice.dto.response.exercise.AddQuestionToExerciseResponse;
import com.example.courseservice.dto.response.exercise.ExerciseDetailResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.ExerciseMapper;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Lesson;
import com.example.courseservice.model.Option;
import com.example.courseservice.model.Question;
import com.example.courseservice.model.compositeKey.OptionID;
import com.example.courseservice.repository.ExerciseRepository;
import com.example.courseservice.repository.LessonRepository;
import com.example.courseservice.repository.OptionRepository;
import com.example.courseservice.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class ExerciseService {
    ExerciseRepository exerciseRepository;
    ExerciseMapper exerciseMapper;

    QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final OptionRepository optionRepository;

//    public Exercise create(Exercise exercise) {};
//    public List<ExerciseResponse> getAllExercises() {
//        return exerciseRepository.findAll().stream().map(exerciseMapper::toExerciseResponse).toList();
//    }

    public ExerciseResponse getExerciseByLessonId(UUID lessonId) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        Exercise exercise = lesson.getExercise();
        if (exercise == null) {
            System.out.println("exercise is null");
            return new ExerciseResponse();
        }

        return exerciseMapper.toExerciseResponse(exercise);

    }

//    public AddQuestionToExerciseResponse addQuestionToExxerciseResponse(UUID exerciseId, UUID questionId) {
//        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow(()->new AppException(ErrorCode.EXERCISE_NOT_FOUND));
//
//        Question question = questionRepository.findById(questionId).orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
//
//        exercise.getQuestionList().add(question);
//        exerciseRepository.save(exercise);
//        return new AddQuestionToExerciseResponse(exerciseId,questionId);
//    }


    public void removeQuestionFromQuiz(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElse(null);
        if (question == null) {
            System.out.println("question is null");
            return;
        }
        questionRepository.delete(question);
    }

    @Transactional
    public ExerciseResponse updateQuiz(ModifyQuizRequest request) {

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (!request.getIsQuizVisible()) {
            lesson.setIsQuizVisible(false);
            lessonRepository.save(lesson);

            Exercise e = lesson.getExercise();
            if (e != null) {
                return exerciseMapper.toExerciseResponse(e);
            }
            return new ExerciseResponse();
        }

        // exercise nếu rỗng hoặc null
        if(request.getQuestions() == null || request.getQuestions().isEmpty()) {
            lesson.setIsQuizVisible(request.getIsQuizVisible());
            lessonRepository.save(lesson);
            Exercise e = lesson.getExercise();
            if (e != null) {
                return exerciseMapper.toExerciseResponse(e);
            }
            return new ExerciseResponse();
        }

        Exercise exercise = lesson.getExercise();
        if (exercise == null) {
            exercise = new Exercise();
            exercise.setLesson(lesson);
        }
        exercise.setQuestionsPerExercise(request.getQuestionsPerExercise());
        exercise.setPassingQuestions(request.getPassingQuestions());

        List<Question> questions = exercise.getQuestionList();

        exercise = exerciseRepository.save(exercise);

        // tạo mới nếu bằng null
        if (questions == null) {
            questions = new ArrayList<>();
        }

        List<UUID> questionIds = new ArrayList<>();
        for (QuestionUpdateRequest q : request.getQuestions()) {
            Question question = null;
            if (q.getQuestionId() != null) {
                question = questionRepository.findById(q.getQuestionId())
                        .orElse(null);
            }
            // tạo mới nếu chưa có trong db
            if (q.getQuestionId() == null || question == null) {
                question = new Question();
                question.setQuestionContent(q.getQuestionContent());
                question.setQuestionType(q.getQuestionType());
                question.setCorrectAnswer(q.getCorrectAnswer());

                // lưu question để lấy id cho option
                question = questionRepository.save(question);

                List<Option> options = new ArrayList<>();
                for (OptionRequest optionRequest : q.getOptionRequests())
                {
                    Option option = new Option();
                    option.setOptionId( OptionID.builder()
                            .questionId(question.getQuestionId())
                            .optionOrder(optionRequest.getOrder())
                            .build());
                    option.setQuestion(question);
                    option.setContent(optionRequest.getContent());
                    options.add(option);
                }
                question.setOptions(options);
//                question = questionRepository.save(question);
                question.setExercise(exercise);
                exercise.getQuestionList().add(question);
                questionIds.add(question.getQuestionId());

//                questionRepository.updateExerciseIdForQuestion(exercise.getExerciseId(), question.getQuestionId());
            }
            else {
                question.setQuestionContent(q.getQuestionContent());
                question.setQuestionType(q.getQuestionType());
                question.setCorrectAnswer(q.getCorrectAnswer());

                List<Option> options = question.getOptions();

                for (OptionRequest optionRequest : q.getOptionRequests()) {
                    Option option = options.stream()
                            .filter(o -> o.getOptionId().getOptionOrder().equals(optionRequest.getOrder()))
                            .findFirst()
                            .orElse(null);
                    if (option == null) {
                        option = new Option();
                        option.setOptionId(OptionID.builder()
                                .questionId(question.getQuestionId())
                                .optionOrder(optionRequest.getOrder())
                                .build());
                        option.setQuestion(question);
                        option.setContent(optionRequest.getContent());
                        options.add(option);
                    }
                    else {
                        option.setContent(optionRequest.getContent());
                        option.setQuestion(question);
                        option.setContent(optionRequest.getContent());

                    }
                }
                question.setOptions(options);
//                question = questionRepository.save(question);
                question.setExercise(exercise);
                exercise.getQuestionList().add(question);

                questionIds.add(question.getQuestionId());

//                questionRepository.updateExerciseIdForQuestion(exercise.getExerciseId(), question.getQuestionId());
            }
        }
        exercise = exerciseRepository.save(exercise);

        lesson.setIsQuizVisible(request.getIsQuizVisible());
        lessonRepository.save(lesson);

        System.out.println(exercise.getExerciseId());
        System.out.println(questionIds);

//        questionRepository.updateExerciseIdForQuestions(exercise.getExerciseId(), questionIds);

//        exercise = exerciseRepository.findById(exercise.getExerciseId())
//                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        return exerciseMapper.toExerciseResponse(exercise);
    }



}
