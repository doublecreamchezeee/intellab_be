package com.example.courseservice.service;

import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.learningLesson.LessonUserResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.LearningLessonMapper;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.mapper.QuestionMapper;
import com.example.courseservice.model.*;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.LearningLessonRepository;
import com.example.courseservice.repository.LessonRepository;
import com.example.courseservice.repository.UserCoursesRepository;
import com.example.courseservice.repository.custom.LearningLessonRepositoryCustom;
import com.example.courseservice.repository.impl.LearningLessonRepositoryCustomImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LessonService {
    LessonRepository lessonRepository;
    LessonMapper lessonMapper;

    LearningLessonRepository learningLessonRepository;
    LearningLessonMapper learningLessonMapper;

    CourseRepository courseRepository;
    UserCoursesRepository userCoursesRepository;

    //@Qualifier("learningLessonRepositoryCustomImpl")
    LearningLessonRepositoryCustomImpl learningLessonRepositoryCustom;
    private final QuestionMapper questionMapper;

    public LessonResponse createLesson(LessonCreationRequest request) {
        if (!courseRepository.existsById(request.getCourseId())) {
            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
        }

        lessonRepository.findByLessonOrderAndCourse_CourseId(request.getLessonOrder(), request.getCourseId())
                .ifPresent(lesson -> {
                    throw new AppException(ErrorCode.LESSON_ORDER_EXISTED);
                });

        Lesson lesson = lessonMapper.toLesson(request);
        Course course = courseRepository.findById(request.getCourseId()).get();
        lesson.setCourse(course);

        lesson = lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(lesson);
    }

    public LessonResponse getLessonById(String lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        learningLessonRepository.findByUserIdAndLesson_LessonId(userId, lesson.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

        return lessonMapper.toLessonResponse(lesson);
    }

    public QuestionResponse getQuestion(UUID lessonId)
    {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        List<Question> questions = lesson.getExercise().getQuestionList();
        if(questions.isEmpty())
            return null;

        Random random = new Random();
        int randomIndex = random.nextInt(questions.size());

        return questionMapper.toQuestionResponse(questions.get(randomIndex));
    }


    public List<LessonResponse> getLessonsByCourseId(String courseId) {
        return lessonRepository.findAllByCourse_CourseId(UUID.fromString(courseId)).stream()
                .map(lessonMapper::toLessonResponse).toList();
    }

    public void deleteLesson(String lessonId) {
        lessonRepository.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lessonRepository.deleteById(UUID.fromString(lessonId));
    }

    public LessonResponse updateLesson(String lessonId, LessonUpdateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        Lesson lesson = lessonRepository.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        lessonMapper.updateLesson(lesson, request);

        lesson.setCourse(course);

        return lessonMapper.toLessonResponse(lessonRepository.save(lesson));
    }

    public LessonResponse addExercise(UUID lessonId, ExerciseCreationRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        Exercise exercise = lesson.getExercise();
        if (exercise == null) {
            exercise = new Exercise();
            exercise.setExercise_name(request.getName());
            exercise.setDescription(request.getDescription());
            lesson.setExercise(exercise);
        }
        else
        {
            throw new AppException(ErrorCode.LESSON_ALREADY_HAD_EXERCISE);
        }
        return lessonMapper.toLessonResponse(lessonRepository.save(lesson));
    }

    public LearningLessonResponse createLearningLesson(UUID userUid, LearningLessonCreationRequest request) {

        Lesson lesson = lessonRepository.findById(request.getLessonId()).orElseThrow(
                () -> new AppException(ErrorCode.LESSON_NOT_FOUND)
        );

        learningLessonRepository.findByLesson_LessonIdAndUserId(
                request.getLessonId(),
                userUid)
            .ifPresent(learningLesson -> {
                throw new AppException(ErrorCode.LEARNING_LESSON_EXISTED);
            }
        );

        userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                lesson.getCourse().getCourseId()
            ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED)
        );

        LearningLesson learningLesson = LearningLesson.builder()
                .lesson(lesson)
                .userId(userUid)
                .status("LEARNING")
                .assignments(new ArrayList<>())
                .build();

        learningLessonRepository.save(learningLesson);

        return learningLessonMapper.toLearningLessonResponse(learningLesson);
    }

    public LearningLessonResponse updateLearningLesson(String learningLessonId,LearningLessonUpdateRequest request) {
        LearningLesson learningLesson = learningLessonRepository.findById(
                UUID.fromString(learningLessonId)
            ).orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND)
        );

        learningLesson.setStatus(request.getStatus());
        learningLesson = learningLessonRepository.save(learningLesson);

        return learningLessonMapper.toLearningLessonResponse(learningLesson);
    }

    /*public List<LessonUserResponse> getLessonProgress(UUID userUid, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        List<LessonUserResponse> learningLessons =
                learningLessonRepository.findAllByUserIdAndLesson_Course_CourseId(userUid, courseId)
                        .stream().map(learningLessonMapper::toLessonUserResponse).toList();

        learningLessonRepository.getLessonProgress(userUid, courseId);
        //learningLessonRepositoryCustom.getLessonProgress(userUid, courseId);
        //List<LessonProgressResponse> learningLessons2 =
        return  learningLessons;
        //List<LessonProgressResponse> learningLessons = learningLessonRepository.getLessonProgress(userUid, courseId);
        //return learningLessons;

        *//*List<LessonUserResponse> learningLessons =
                learningLessonRepository.findAllByUserIdAndLesson_Course_CourseId(userUid, courseId)
                        .stream().map(learningLessonMapper::toLessonUserResponse).toList();
*//*
        //learningLessonRepository.getLessonProgress(userUid, courseId);
    }*/

    public List<LessonProgressResponse> getLessonProgress(UUID userUid, UUID courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        return learningLessonRepositoryCustom.getLessonProgress(userUid, courseId);

    }

}
