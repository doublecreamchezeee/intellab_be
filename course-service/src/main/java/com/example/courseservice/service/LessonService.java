package com.example.courseservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.courseservice.client.AiServiceClient;
import com.example.courseservice.client.ProblemClient;
import com.example.courseservice.configuration.DotenvConfig;
import com.example.courseservice.constant.PredefinedLearningStatus;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
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
import com.example.courseservice.model.compositeKey.OptionID;
import com.example.courseservice.repository.*;
import com.example.courseservice.specification.LessonSpecification;
import com.example.courseservice.utils.ParseUUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

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
    private final QuestionRepository questionRepository;
    private final ExerciseRepository exerciseRepository;
    private final OptionRepository optionRepository;
    AiServiceClient aiServiceClient;

    public LessonResponse getLessonInformation(UUID lessonId){
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        return lessonMapper.toLessonResponse(lesson);
    }

    public List<LessonResponse> getCourseLessons(UUID courseId){
        boolean exits = courseRepository.existsById(courseId);

        if (!exits) {
            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
        }

        List<Lesson> lessons = lessonRepository.findAllByCourse_CourseIdOrderByLessonOrderAsc(courseId);
        return lessons.stream().map(lessonMapper::toLessonResponse).collect(Collectors.toList());
    }

    public LessonResponse createBlankLesson(UUID courseId){
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
        }
        Lesson lesson = new Lesson();

        Course course = courseRepository.findById(courseId).get();

        lesson.setCourse(course);

        if (course.getLessons() != null && !course.getLessons().isEmpty())
        {
            lesson.setLessonOrder(course.getLessons().size() + 1);
        } else {
            lesson.setLessonOrder(1);
        }

        lesson = lessonRepository.save(lesson);

        if (course.getCurrentCreationStep() < 2)
        {
            course.setCurrentCreationStep(2);
            courseRepository.save(course);
        }

        return lessonMapper.toLessonResponse(lesson);
    }

    @Transactional
    public LessonResponse copyLesson(UUID lessonId, UUID courseId){
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                ()-> new AppException(ErrorCode.LESSON_NOT_FOUND)
        );

        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
        );

        Lesson newLesson = new Lesson();
        // copy thuộc tính
        newLesson.setCourse(course);
        newLesson.setLessonName(lesson.getLessonName());
        newLesson.setDescription(lesson.getDescription());
        newLesson.setContent(lesson.getContent());
        newLesson.setProblemId(lesson.getProblemId());

        if (course.getLessons() != null && !course.getLessons().isEmpty())
        {
            newLesson.setLessonOrder(course.getLessons().size() + 1);
        } else {
            newLesson.setLessonOrder(1);
        }

        newLesson = lessonRepository.save(newLesson);

        if (course.getCurrentCreationStep() < 2)
        {
            course.setCurrentCreationStep(2);
            courseRepository.save(course);
        }

        if (lesson.getExercise() == null)
        {
           throw new AppException(ErrorCode.EXERCISE_NOT_FOUND);
        }

        Exercise cloneQuiz = cloneQuiz(lesson.getExercise(), newLesson);

        newLesson.setExercise(cloneQuiz);

        sendRequestToInsertNewLessonVectorEmbedding(newLesson.getLessonId());

        return lessonMapper.toLessonResponse(newLesson);
    }

    @Transactional
    protected Exercise cloneQuiz(Exercise source, Lesson lesson){

        Exercise newExercise = new Exercise();
        newExercise.setDescription(source.getDescription());
        newExercise.setExerciseName(source.getExerciseName());
        newExercise.setPassingQuestions(source.getPassingQuestions());
        newExercise.setQuestionsPerExercise(source.getQuestionsPerExercise());
        newExercise.setLesson(lesson);
        newExercise = exerciseRepository.save(newExercise);

        newExercise = exerciseRepository.findById(newExercise.getExerciseId()).orElse(null);

        if (newExercise == null)
        {
            System.out.println("Couldn't find exercise with id ");
            return null;
        }

        List<UUID> questionIds = new ArrayList<>();
        for (Question question:source.getQuestionList())
        {
            Question newQuestion = new Question();
            newQuestion.setCorrectAnswer(question.getCorrectAnswer());
            newQuestion.setQuestionId(UUID.randomUUID());
            newQuestion.setQuestionContent(question.getQuestionContent());
            newQuestion.setQuestionType(question.getQuestionType());

            List<Category> categories = question.getCategories();
            if (categories != null && !categories.isEmpty())
            {
                for (Category c : categories)
                {
                    newQuestion.getCategories().add(c);
                }
            }

            for(Option option:question.getOptions())
            {
                Option cloneOption = new Option();
                cloneOption.setContent(option.getContent());
                cloneOption.setOptionId(
                        OptionID
                                .builder()
                                .questionId(newQuestion.getQuestionId())
                                .optionOrder(option.getOptionId().getOptionOrder())
                                .build()
                );
                cloneOption.setQuestion(newQuestion);
                newQuestion.getOptions().add(cloneOption);
            }
            newQuestion = questionRepository.save(newQuestion);
            questionIds.add(newQuestion.getQuestionId());
//            newExercise.getQuestionList().add(newQuestion);
        }
        newExercise = exerciseRepository.save(newExercise);

        System.out.println(questionIds);
        questionRepository.updateExerciseIdForQuestions(newExercise.getExerciseId(),questionIds);
        return newExercise;
    }

    public Boolean updateLessonsOrder(UUID courseId, List<UUID> lessonIds) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

            Map<UUID, Integer> lessonIdMap = IntStream.range(0, lessonIds.size()).boxed().collect(Collectors.toMap(lessonIds::get, i -> i + 1));

            List<Lesson> lessons = lessonRepository.findAllById(lessonIds);

            for (Lesson lesson : lessons) {
                if (!lesson.getCourse().getCourseId().equals(courseId)){
                    System.out.println("Lesson is not in course");
                   throw new AppException(ErrorCode.INVALID_LESSON_COURSE);
                }
                lesson.setLessonOrder(lessonIdMap.get(lesson.getLessonId()));
            }
            lessonRepository.saveAll(lessons);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public LessonResponse updateLesson(LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId()).orElseThrow(
                () -> new AppException(ErrorCode.LESSON_NOT_FOUND)
        );

        lesson.setLessonName(request.getLessonName());
        //lesson.setLessonOrder(request.getLessonOrder());
        lesson.setContent(request.getContent());
        lesson.setDescription(request.getDescription());
        lesson.setProblemId(request.getProblemId());
        lesson = lessonRepository.save(lesson);

        sendRequestToUpdateExistedLessonVectorEmbedding(lesson.getLessonId());

        return lessonMapper.toLessonResponse(lesson);
    }

    public void removeLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                () -> new AppException(ErrorCode.LESSON_NOT_FOUND)
        );

        Course course = lesson.getCourse();
        Integer lessonOrder = lesson.getLessonOrder();

        Exercise exercise = lesson.getExercise();

        if (exercise != null) {
            for (Question question : exercise.getQuestionList()) {
                optionRepository.deleteAll(question.getOptions());
            }
            questionRepository.deleteAll(exercise.getQuestionList());
            exerciseRepository.delete(exercise);
        }

        lessonRepository.delete(lesson);

        sendRequestToDeleteLessonVectorEmbedding(lessonId);

        lessonRepository.flush();

        Specification<Lesson> specification = Specification.where(
                LessonSpecification.greaterThanLessonOrder(lessonOrder)
                        .and(LessonSpecification.hasCourseId(course.getCourseId())));
        // Update the lesson order for the remaining lessons in the course
        List<Lesson> lessons = lessonRepository.findAll(specification);
        for (Lesson lesson1 : lessons) {
            lesson1.setLessonOrder(lessonOrder++);
        }
        lessonRepository.saveAll(lessons);
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

        UUID exerciseId = null;

        if (lesson.getIsQuizVisible() && lesson.getExercise() != null )
        {
            exerciseId = lesson.getExercise().getExerciseId();
        }

        // Build the response with all the lesson details

        return DetailsLessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .content(lesson.getContent())
                .description(lesson.getDescription())
                .lessonOrder(lesson.getLessonOrder())
                .lessonName(lesson.getLessonName())
                .courseId(lesson.getCourse().getCourseId())
                .exerciseId(exerciseId) // Check if exercise is null
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
        Exercise exercise = assignments.get(0).getExercise();

        Assignment lastEdited = null;

        for( Assignment assignment:assignments)
        {
            if (lastEdited == null)
                lastEdited = assignment;
            else if (lastEdited.getScore() >= exercise.getPassingQuestions())
            {
                if(assignment.getScore()>=lastEdited.getScore())
                    lastEdited = assignment;
            }
            else if (lastEdited.getScore() < exercise.getPassingQuestions())
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

        numberOfQuestions = exercise.getQuestionsPerExercise();
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

//    public LessonResponse updateLesson(String lessonId, LessonUpdateRequest request) {
//        Course course = courseRepository.findById(request.getCourseId())
//                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
//
//        Lesson lesson = lessonRepository.findById(UUID.fromString(lessonId))
//                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
//
//        lessonMapper.updateLesson(lesson, request);
//
//        lesson.setCourse(course);
//
//        return lessonMapper.toLessonResponse(lessonRepository.save(lesson));
//    }
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

        Lesson lesson = learningLesson.getLesson();
        if (lesson.getExercise() == null || !lesson.getIsQuizVisible()) {
            learningLesson.setIsDoneTheory(true);
        }

        learningLesson.setIsDoneTheory(null);

        if (learningLesson.getIsDonePractice() != null
                && learningLesson.getIsDonePractice() && learningLesson.getIsDoneTheory() != null) {
            learningLesson.setStatus(PredefinedLearningStatus.DONE);
        }

        learningLessonRepository.save(learningLesson);

        DetailCourseResponse detailCourseResponse = courseService.getCourseById(courseId, userUid);

        userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
        userCourses = userCoursesRepository.save(userCourses);

        if (abs(userCourses.getProgressPercent()-100) <= 1e-5f ) {
            userCourses.setStatus(PredefinedLearningStatus.DONE);
            userCourses = userCoursesRepository.save(userCourses);
            if (userCourses.getCertificate() == null) {
                System.out.println("createCertificate");
                System.out.println(userCourses.getCertificate());
                courseService.createCertificate(courseId, userUid);
            }
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
        userCourses = userCoursesRepository.save(userCourses);


        if (abs(userCourses.getProgressPercent()-100) <= 1e-5f ) {
            userCourses.setStatus(PredefinedLearningStatus.DONE);
            userCourses = userCoursesRepository.save(userCourses);
            if (userCourses.getCertificate() == null) {
                System.out.println("createCertificate");
                System.out.println(userCourses.getCertificate());
                courseService.createCertificate(courseId, userUid);
            }
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
            userCourses = userCoursesRepository.save(userCourses);

            if (abs(userCourses.getProgressPercent()-100) <= 1e-5f) {
                userCourses.setStatus(PredefinedLearningStatus.DONE);
                userCourses = userCoursesRepository.save(userCourses);
                if (userCourses.getCertificate() == null) {
                    System.out.println("createCertificate");
                    System.out.println(userCourses.getCertificate());
                    courseService.createCertificate(userCourses.getEnrollId().getCourseId(), userUid);
                }
            }
        }

        return true;

    }

    public Boolean completeAllLessonByCourseId(
            UUID courseId,
            UUID userUid
    ) {
        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                courseId
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

        List<Lesson> lessons = lessonRepository.findAllByCourse_CourseId(courseId);

        for (Lesson lesson : lessons) {

            Optional<LearningLesson> learningLessonOptional = learningLessonRepository.findByLesson_LessonIdAndUserId(lesson.getLessonId(), userUid);

            if (learningLessonOptional.isEmpty()) {
                continue;
            }

            LearningLesson learningLesson = learningLessonOptional.get();

            learningLesson.setIsDonePractice(true);
            learningLesson.setIsDoneTheory(true);
            learningLesson.setStatus(PredefinedLearningStatus.DONE);

            learningLessonRepository.save(learningLesson);

        }

        DetailCourseResponse detailCourseResponse = courseService.getCourseById(courseId, userUid);

        userCourses.setProgressPercent(detailCourseResponse.getProgressPercent());
        userCourses.setStatus("Done");

        /*try {
            courseService.createCertificate(courseId, userUid);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        userCoursesRepository.save(userCourses);

        return true;
    }

    public Boolean restartAllLessonByCourseId(
            UUID courseId,
            UUID userUid
    ) {
        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(
                userUid,
                courseId
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_ENROLLED));

        List<Lesson> lessons = lessonRepository.findAllByCourse_CourseId(courseId);

        for (Lesson lesson : lessons) {
            Optional<LearningLesson> learningLessonOptional = learningLessonRepository.findByLesson_LessonIdAndUserId(lesson.getLessonId(), userUid);

            if (learningLessonOptional.isEmpty()) {
                continue;
            }

            LearningLesson learningLesson = learningLessonOptional.get();

            learningLesson.setIsDonePractice(false);
            learningLesson.setIsDoneTheory(false);
            learningLesson.setStatus(PredefinedLearningStatus.LEARNING);

            learningLessonRepository.save(learningLesson);
        }

        userCourses.setProgressPercent(0.0F);
        userCourses.setStatus("Learning");

        userCoursesRepository.save(userCourses);

        return true;
    }

    public String uploadFile(MultipartFile file, UUID courseId, UUID userId) throws IOException {
        Cloudinary cloudinary = new Cloudinary(DotenvConfig.get("CLOUDINARY_URL"));
        String path = "CourseImages/"+ userId + "/" + courseId;
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", path));
        return uploadResult.get("secure_url").toString(); // Trả về đường dẫn ảnh đã upload
    }

    @Async
    public void sendRequestToInsertNewLessonVectorEmbedding(UUID lessonId) {
        try {
            aiServiceClient.insertLessonEmbeddingData(lessonId).block();
        } catch (Exception e) {
            log.error("Error while sending request to insert new lesson vector embedding: {}", e.getMessage());
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    @Async
    public void sendRequestToUpdateExistedLessonVectorEmbedding(UUID lessonId) {
        try {
            aiServiceClient.updateLessonEmbeddingData(lessonId).block();
        } catch (Exception e) {
            log.error("Error while sending request to insert new lesson vector embedding: {}", e.getMessage());
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

    @Async
    public void sendRequestToDeleteLessonVectorEmbedding(UUID lessonId) {
        try {
            aiServiceClient.deleteLessonEmbeddingData(lessonId).block();
        } catch (Exception e) {
            log.error("Error while sending request to delete lesson vector embedding: {}", e.getMessage());
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }

}


