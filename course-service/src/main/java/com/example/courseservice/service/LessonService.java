package com.example.courseservice.service;

import com.example.courseservice.client.ProblemClient;
import com.example.courseservice.constant.PredefinedLearningStatus;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.AssignmentDetailMapper;
import com.example.courseservice.mapper.LearningLessonMapper;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.mapper.QuestionMapper;
import com.example.courseservice.model.*;
import com.example.courseservice.repository.*;
import com.example.courseservice.repository.custom.DetailsLessonRepositoryCustom;
import com.example.courseservice.repository.impl.DetailsCourseRepositoryCustomImpl;
import com.example.courseservice.repository.impl.LearningLessonRepositoryCustomImpl;
import com.example.courseservice.utils.ParseUUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    AssignmentDetailMapper assignmentDetailMapper;


    //@Qualifier("learningLessonRepositoryCustomImpl")
    LearningLessonRepositoryCustomImpl learningLessonRepositoryCustom;
    DetailsLessonRepositoryCustom detailsLessonRepositoryCustom;
    private final QuestionMapper questionMapper;
    ProblemClient problemClient;
    AssignmentRepository assignmentRepository;
    DetailsCourseRepositoryCustomImpl detailsCourseRepositoryCustom;

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


    //Quy trình flow làm bài tập: Get quiz -> làm bài
    // subflow1-làm chưa xong rồi thoát ra: Post Assignment với score là null(chưa làm xong), các câu chưa làm thì
    //có answer của assignmentDetail là null.
    // subflow2-làm xong rồi nộp: Post Assignment với score là kết quả làm được và list<assignmentDetail> là các câu
    // trong bài làm.
    //Hàm getQuesstion: Trả về list QuestionResponse
    // trường hợp đã làm trước đó:
    // - Trả về bài làm đạt đủ điểm
    // - Trả về bài làm gần đây nhất nếu chưa có bài nào đạt đủ điểm.
    // trường hoợp chưa làm: trả về danh sách câu hỏi
    public List<QuestionResponse> getLastAssignment(UUID lessonId, UUID userId) {
        LearningLesson learningLesson = learningLessonRepository.findByLesson_LessonIdAndUserId(lessonId,userId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        List<Assignment> assignments = learningLesson.getAssignments();
        // nếu chưa có bài làm nào thì bỏ qua
        if(assignments.isEmpty()){
            throw new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        Assignment lastEdited = null;

        for( Assignment assignment:assignments)
        {
            if (lastEdited == null)
                lastEdited = assignment;
            else if (lastEdited.getScore() >= 8)
            {
                if(assignment.getScore()>=lastEdited.getScore())
                    lastEdited = assignment;
            }
            else if (lastEdited.getScore() < 8)
                lastEdited = assignment;
        }

        List<AssignmentDetail> assignmentDetails = lastEdited.getAssignment_details();
        return assignmentDetails.stream()
                .map(assignmentDetailMapper::toQuestionResponse)
                .collect(Collectors.toList());

    }


    public List<QuestionResponse> getQuestion(UUID lessonId, Integer numberOfQuestions)
    {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        Exercise exercise = lesson.getExercise();
        if (exercise == null)
            return null;
        List<Question> questions = exercise.getQuestionList();

        if(questions == null || questions.isEmpty())
            return null;

        if(numberOfQuestions >= questions.size())
            return questions.stream()
                    .map(questionMapper::toQuestionResponse)
                    .collect(Collectors.toList());

        Collections.shuffle(questions);

        return questions.subList(0,numberOfQuestions)
                .stream().map(questionMapper::toQuestionResponse)
                .collect(Collectors.toList());

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

    public LearningLessonResponse updateLearningLesson(
            UUID learningLessonId,
            UUID courseId,
            String userUid,
            LearningLessonUpdateRequest request) {

        LearningLesson learningLesson = learningLessonRepository.findById(
                learningLessonId
            ).orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND)
        );

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                ParseUUID.normalizeUID(userUid),
                courseId
            ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED)
        );

        learningLesson.setStatus(request.getStatus());
        learningLesson.setLastAccessedDate(new Date().toInstant());
        learningLesson = learningLessonRepository.save(learningLesson);

        // save latest lesson id
        userCourses.setLatestLessonId(learningLesson.getLesson().getLessonId());
        userCoursesRepository.save(userCourses);

        return learningLessonMapper.toLearningLessonResponse(learningLesson);
    }

    public Page<LessonProgressResponse> getLessonProgress(UUID userUid, UUID courseId, Pageable pageable) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        return learningLessonRepositoryCustom.getLessonProgress(userUid, courseId, pageable);

    }

    public Boolean doneTheoryOfLesson(UUID learningLessonId,
                                      UUID courseId,
                                      UUID userUid) {
        LearningLesson learningLesson = learningLessonRepository.findById(learningLessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                courseId
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

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

        if (learningLesson.getIsDonePractice() != null
                && learningLesson.getIsDonePractice()==true) {
            learningLesson.setStatus(PredefinedLearningStatus.DONE);
        }

        learningLessonRepository.save(learningLesson);

        DetailCourseResponse detailCourseResponse = detailsCourseRepositoryCustom
                .getDetailsCourse(courseId, userUid)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
        userCoursesRepository.save(userCourses);

        return  true;


    }

    public Boolean donePracticeOfLesson(UUID learningLessonId,
                                        UUID courseId,
                                        UUID userUid) {
        LearningLesson learningLesson = learningLessonRepository.findById(learningLessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                courseId
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

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

        if (learningLesson.getIsDoneTheory() != null
                && learningLesson.getIsDoneTheory()==true) {
            learningLesson.setStatus(PredefinedLearningStatus.DONE);
        }

        learningLessonRepository.save(learningLesson);

        DetailCourseResponse detailCourseResponse = detailsCourseRepositoryCustom
                .getDetailsCourse(courseId, userUid)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
        userCoursesRepository.save(userCourses);

        return true;
    }

}


