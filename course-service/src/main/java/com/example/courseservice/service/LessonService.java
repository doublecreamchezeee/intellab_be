package com.example.courseservice.service;

import com.example.courseservice.client.ProblemClient;
import com.example.courseservice.constant.PredefinedLearningStatus;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.Option.OptionResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.*;
import com.example.courseservice.model.*;
import com.example.courseservice.repository.*;
import com.example.courseservice.utils.ParseUUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    OptionMapper optionMapper;


    //@Qualifier("learningLessonRepositoryCustomImpl")
    private final QuestionMapper questionMapper;
    ProblemClient problemClient;
    AssignmentRepository assignmentRepository;
    private final CourseService courseService;

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


    public Lesson getNextLessonId(Lesson lesson) {
        return lessonRepository.findByLessonOrderAndCourse_CourseId(lesson.getLessonOrder()+1, lesson.getCourse().getCourseId()).orElse(null);
    }


    public DetailsLessonResponse getLessonById(UUID lessonId, UUID userId) {
        // Fetch the lesson by its ID
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // Fetch the LearningLesson entity for this user and lesson
        LearningLesson learningLesson = learningLessonRepository.findByLesson_LessonIdAndUserId(lessonId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

        // Check if theory and practice are done
        Boolean isDoneTheory = learningLesson.getIsDoneTheory();
        Boolean isDonePractice = learningLesson.getIsDonePractice();

        // Get the next lesson ID and lesson name
        Lesson nextLesson =  getNextLessonId(lesson);
        UUID nextLessonId = (nextLesson != null) ? nextLesson.getLessonId() : null;
        String nextLessonName = (nextLesson != null) ? nextLesson.getLessonName() : null;

        // Build the response with all the lesson details

        return DetailsLessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .content(lesson.getContent())
                .description(lesson.getDescription())
                .lessonOrder(lesson.getLessonOrder())
                .lessonName(lesson.getLessonName())
                .courseId(lesson.getCourse().getCourseId())
                .exerciseId(lesson.getExercise() != null ? lesson.getExercise().getExerciseId() : null) // Check if exercise is null
                .problemId(lesson.getProblemId())
                .learningId(learningLesson.getLearningId())
                .nextLessonId(nextLessonId)
                .nextLessonName(nextLessonName)
                .isDoneTheory(isDoneTheory)
                .isDonePractice(isDonePractice)
                .build();
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

        List<AssignmentDetail> assignmentDetails = lastEdited.getAssignmentDetails();
        return assignmentDetails.stream()
                .map(assignmentDetail -> {
                    QuestionResponse response = assignmentDetailMapper.toQuestionResponse(assignmentDetail);
                    Question question = assignmentDetail.getQuestion();
                    List<OptionResponse> options = question.getOptions().stream().map(optionMapper::toResponse).toList();
                    response.setOptions(options);

                    return response;
                })
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
            exercise.setExerciseName(request.getName());
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
        // Ensure that the course exists before proceeding
        courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Fetch paginated lessons for the course, sorted by lessonOrder
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.asc("lessonOrder")));
        Page<Lesson> lessonsPage = lessonRepository.findByCourse_CourseId(courseId, sortedPageable);

        // Fetch learning lessons for the user and the given course
        List<LearningLesson> learningLessons = learningLessonRepository.findAllByUserIdAndLesson_Course_CourseId(userUid, courseId);

        // Map lessons to LessonProgressResponse and return a paginated result
        return lessonsPage.map(lesson -> {
            // Find the corresponding learning lesson for the user
            Optional<LearningLesson> learningLessonOpt = learningLessons.stream()
                    .filter(ll -> ll.getLesson().getLessonId().equals(lesson.getLessonId()))
                    .findFirst();

            // Use the builder to create a LessonProgressResponse object
            return LessonProgressResponse.builder()
                    .learningId(learningLessonOpt.map(LearningLesson::getLearningId).orElse(null))
                    .lessonId(lesson.getLessonId())
                    .courseId(lesson.getCourse().getCourseId())
                    .lessonOrder(lesson.getLessonOrder())
                    .lessonName(lesson.getLessonName())
                    .description(lesson.getDescription())
                    .content(lesson.getContent())
                    .problemId(lesson.getProblemId())
                    .exerciseId(lesson.getExercise() != null ? lesson.getExercise().getExerciseId() : null)
                    .status(learningLessonOpt.map(LearningLesson::getStatus).orElse("LEARNING"))
                    .lastAccessedDate(learningLessonOpt.map(LearningLesson::getLastAccessedDate).orElse(null))
                    .isDoneTheory(learningLessonOpt.map(LearningLesson::getIsDoneTheory).orElse(false))
                    .isDonePractice(learningLessonOpt.map(LearningLesson::getIsDonePractice).orElse(false))
                    .build();
        });
    }



    public Boolean doneTheoryOfLesson(UUID learningLessonId,
                                      UUID courseId,
                                      UUID userUid) throws Exception {
        LearningLesson learningLesson = learningLessonRepository.findById(learningLessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                courseId
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

//        Lesson lesson = learningLesson.getLesson();

        learningLesson.setIsDoneTheory(true);

        if (learningLesson.getIsDonePractice() != null
                && learningLesson.getIsDonePractice()) {
            learningLesson.setStatus(PredefinedLearningStatus.DONE);
        }

        learningLessonRepository.save(learningLesson);

        DetailCourseResponse detailCourseResponse = courseService.getCourseById(courseId, userUid);

        userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
        userCoursesRepository.save(userCourses);

        if (userCourses.getProgressPercent()-100 <= 1e-6f)
        {
            courseService.createCertificate(courseId,userUid);
        }
        return true;
    }

    public Boolean donePracticeOfLesson(UUID learningLessonId,
                                        UUID courseId,
                                        UUID userUid) throws Exception {
        LearningLesson learningLesson = learningLessonRepository.findById(learningLessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                courseId
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

//        Lesson lesson = learningLesson.getLesson();

        learningLesson.setIsDonePractice(true);

        if (learningLesson.getIsDoneTheory() != null
                && learningLesson.getIsDoneTheory()) {
            learningLesson.setStatus(PredefinedLearningStatus.DONE);
        }

        learningLessonRepository.save(learningLesson);

        DetailCourseResponse detailCourseResponse = courseService.getCourseById(courseId, userUid);

        userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
        userCoursesRepository.save(userCourses);
        if (userCourses.getProgressPercent()-100 <= 1e-6f)
        {
            courseService.createCertificate(courseId,userUid);
        }

        return true;
    }

    public Boolean donePracticeOfLessonByProblemId(UUID problemId, UUID userUid) throws Exception {
        List<Lesson> lessons = lessonRepository.findAllByProblemId(problemId);

        for (Lesson lesson : lessons) {
            Optional<LearningLesson> learningLessonOptional = learningLessonRepository.findByLesson_LessonIdAndUserId(lesson.getLessonId(), userUid);

            if (!learningLessonOptional.isPresent()) {
                continue;
            }

            LearningLesson learningLesson = learningLessonOptional.get();

            UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                    userUid,
                    lesson.getCourse().getCourseId()
            ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

            learningLesson.setIsDonePractice(true);

            if (learningLesson.getIsDoneTheory() != null
                    && learningLesson.getIsDoneTheory()) {
                learningLesson.setStatus(PredefinedLearningStatus.DONE);
            }

            learningLessonRepository.save(learningLesson);

            DetailCourseResponse detailCourseResponse = courseService.getCourseById(lesson.getCourse().getCourseId(), userUid);

            userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
            userCoursesRepository.save(userCourses);

            if (userCourses.getProgressPercent()-100 <= 1e-6f)
            {
                courseService.createCertificate(userCourses.getEnrollId().getCourseId(),userUid);
            }
        }

        return true;

    }

}


