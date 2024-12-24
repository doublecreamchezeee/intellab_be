package com.example.courseservice.service;

import com.example.courseservice.client.ProblemClient;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.LearningLessonMapper;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.mapper.QuestionMapper;
import com.example.courseservice.model.*;
import com.example.courseservice.repository.*;
import com.example.courseservice.repository.custom.DetailsLessonRepositoryCustom;
import com.example.courseservice.repository.impl.LearningLessonRepositoryCustomImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

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
    DetailsLessonRepositoryCustom detailsLessonRepositoryCustom;
    private final QuestionMapper questionMapper;
    ProblemClient problemClient;
    AssignmentRepository assignmentRepository;

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

    public DetailsLessonResponse getLessonById(String lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        /*learningLessonRepository.findByUserIdAndLesson_LessonId(userId, lesson.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));*/

        DetailsLessonResponse detailsLessonResponse = detailsLessonRepositoryCustom
                .getDetailsLesson(lesson.getLessonId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

        /*List<DetailsProblemSubmissionResponse> detailsProblemSubmissionResponse
                //check null problemId
                = problemClient.getSubmissionDetailsByProblemIdAndUserUid(lesson.getProblemId(), userId).block();
        System.out.println("detailsProblemSubmissionResponse: " + detailsProblemSubmissionResponse);
        if (detailsProblemSubmissionResponse != null) {
            detailsLessonResponse.setIsDonePractice(true);
        }*/
        return detailsLessonResponse;
        //return lessonMapper.toLessonResponse(lesson);
    }

    public QuestionResponse getQuestion(UUID lessonId)
    {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        Exercise exercise = lesson.getExercise();
        if (exercise == null)
            return null;
        List<Question> questions = exercise.getQuestionList();

        if(questions.isEmpty()||questions==null)
            return null;


        Random random = new Random();
        int randomIndex = random.nextInt(questions.size());

        return questionMapper.toQuestionResponse(questions.get(randomIndex));
    }

    public Page<LessonResponse> getLessonsByCourseId(String courseId, Pageable pageable) {
        Page<Lesson> lessons =  lessonRepository.findAllByCourse_CourseIdOrderByLessonOrder(
                UUID.fromString(courseId),
                pageable
            );

        return lessons.map(lessonMapper::toLessonResponse);
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
        learningLesson.setLastAccessedDate(new Date().toInstant());
        learningLesson = learningLessonRepository.save(learningLesson);

        return learningLessonMapper.toLearningLessonResponse(learningLesson);
    }

    public List<LessonProgressResponse> getLessonProgress(UUID userUid, UUID courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        return learningLessonRepositoryCustom.getLessonProgress(userUid, courseId);

    }

    public Boolean doneTheoryOfLesson(UUID learningLessonId) {
        LearningLesson learningLesson = learningLessonRepository.findById(learningLessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        Lesson lesson = learningLesson.getLesson();

        //TODO: DONT DELETE THIS COMMENTED CODE
        // case lesson don't have exercise
       /*if (lesson.getExercise() == null) {
            // check if existed empty assignment return true, else create new empty assignment
            List<Assignment> existedAssignment = assignmentRepository.findByLearningLesson_LearningId(learningLessonId);

            if (!existedAssignment.isEmpty() && learningLesson.getIsDoneTheory() != null && learningLesson.getIsDoneTheory()) {
              return true;
            } else {
              Assignment newAssignment = Assignment.builder()
                      .learningLesson(learningLesson)
                      .submit_order(0)
                      .score(0f)
                      .build();

              assignmentRepository.save(newAssignment);

              learningLesson.setIsDoneTheory(true);
              learningLessonRepository.save(learningLesson);

              return true;
            }
       }
       else {
           Boolean checkIsDone = learningLessonRepositoryCustom.markTheoryLessonAsDone(
                   learningLessonId,
                   lesson.getExercise().getExercise_id()
           );
           learningLesson.setIsDoneTheory(checkIsDone);
           learningLessonRepository.save(learningLesson);

           return checkIsDone;
       }*/

        learningLesson.setIsDoneTheory(true);
        learningLessonRepository.save(learningLesson);
        return  true;


    }

    public Boolean donePracticeOfLesson(UUID learningLessonId) {
        LearningLesson learningLesson = learningLessonRepository.findById(learningLessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        Lesson lesson = learningLesson.getLesson();

        //TODO: DONT DELETE THIS COMMENTED CODE
        // case lesson don't have problem
        /*if (lesson.getProblemId() == null) {
            learningLesson.setIsDonePractice(true);
            learningLessonRepository.save(learningLesson);
            return true;
        }
        else {
            List<DetailsProblemSubmissionResponse> detailsProblemSubmissionResponse =
                    problemClient.getSubmissionDetailsByProblemIdAndUserUid(
                            lesson.getProblemId(),
                            learningLesson.getUserId()
                    ).block();

            System.out.println("detailsProblemSubmissionResponse: " + detailsProblemSubmissionResponse);

            if (detailsProblemSubmissionResponse != null) {
                learningLesson.setIsDonePractice(true);
                learningLessonRepository.save(learningLesson);
                return true;
            }

            learningLesson.setIsDonePractice(false);
            learningLessonRepository.save(learningLesson);
            return false;
        }*/
        learningLesson.setIsDonePractice(true);
        learningLessonRepository.save(learningLesson);
        return true;
    }

}


