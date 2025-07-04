package com.example.courseservice.service;

import com.example.courseservice.annotation.ExecutionTiming;
import com.example.courseservice.client.AiServiceClient;
import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.constant.PredefinedLearningStatus;
import com.example.courseservice.constant.PredefinedRole;
import com.example.courseservice.dto.request.course.*;
import com.example.courseservice.dto.request.notification.NotificationRequest;
import com.example.courseservice.dto.response.auth.ValidatedTokenResponse;
import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.dto.response.course.*;
import com.example.courseservice.dto.response.userCourses.CertificateCreationResponse;
import com.example.courseservice.dto.response.userCourses.CompleteCourseResponse;
import com.example.courseservice.dto.response.userCourses.EnrolledCourseResponse;
import com.example.courseservice.enums.account.PremiumPackage;
import com.example.courseservice.enums.userCourse.UserCourseAccessStatus;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CategoryMapper;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.mapper.LessonMapper;
import com.example.courseservice.model.*;
import com.example.courseservice.model.Firestore.User;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import com.example.courseservice.repository.*;
import com.example.courseservice.specification.CourseSpecification;
import com.example.courseservice.specification.LessonSpecification;
import com.example.courseservice.specification.UserCoursesSpecification;
import com.example.courseservice.utils.Certificate.CertificateTemplate1;
import com.example.courseservice.utils.Certificate.CertificateTemplate2;
import com.example.courseservice.utils.ParseUUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springdoc.core.parsers.ReturnTypeParser;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseService {
    CourseRepository courseRepository;
    CourseMapper courseMapper;
    UserCoursesRepository userCoursesRepository;
    LessonRepository lessonRepository;
    LearningLessonRepository learningLessonRepository;
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    private final IdentityClient identityClient;
    CertificateRepository certificateRepository;
    private final FirestoreService firestoreService;
    private final LessonMapper lessonMapper;
    private final NotificationService notificationService;
    CloudinaryService cloudinaryService;
    TopicRepository topicRepository;
    CourseSummaryRepository courseSummaryRepository;
    AiServiceClient aiServiceClient;

    public Page<CourseCreationResponse> getAllCourses(
            Boolean isAvailable, Boolean isCompletedCreation,
            Pageable pageable
    ) {
        Specification<Course> specification = Specification.where(
                CourseSpecification.isAvailableSpecification(isAvailable)
                        .and(CourseSpecification.isCompletedCreationSpecification(isCompletedCreation))
        );

        Page<Course> courses = courseRepository.findAll(specification, pageable);

        return courses.map(course -> {
            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
            List<Section> sections = course.getSections();
            CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
            response.setLessonCount(lessonCount);
            response.setSections(sections);
            return response;
        });
    }

    public Page<AdminCourseCreationResponse> getAllCoursesOfAdmin(
            Boolean isAvailable, Boolean isCompletedCreation,
            UUID userUuid, Pageable pageable
    ) {
        Specification<Course> specification = Specification.where(
                CourseSpecification.isAvailableSpecification(isAvailable)
                        .and(CourseSpecification.isCompletedCreationSpecification(isCompletedCreation))
                        .and(CourseSpecification.userIdSpecification(userUuid))
        );

        Page<Course> courses = courseRepository.findAll(specification, pageable);

        return courses.map(course -> {
            AdminCourseCreationResponse response = courseMapper.toAdminCourseCreationResponse(course);

            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
            response.setLessonCount(lessonCount);

            List<Section> sections = course.getSections();
            response.setSections(sections);

            int numberOfEnrolledStudents = course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0;
            response.setNumberOfEnrolledStudents(numberOfEnrolledStudents);

            return response;
        });
    }

    private <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

    public Page<CourseCreationResponse> getAllByCategory(
            Integer section, Boolean isAvailable,
            Boolean isCompletedCreation, Pageable pageable
    ) {

        Specification<Course> courseSpecification = Specification.where(
                CourseSpecification.isAvailableSpecification(isAvailable)
                        .and(CourseSpecification.sectionSpecification(section))
                        .and(CourseSpecification.isCompletedCreationSpecification(isCompletedCreation))
        );

        Page<Course> result = courseRepository.findAll(courseSpecification, pageable);

        //Page<Course> result = courseRepository.findAllBySections_Id(section, pageable);

        return result.map(
                course -> {
                    int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
                    List<Section> sections = course.getSections();
                    CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
                    response.setLessonCount(lessonCount);
                    response.setSections(sections);
                    return response;
                }
        );
    }

    public Page<AdminCourseCreationResponse> getAllCoursesOfAdminByCategory(
            Integer section, Boolean isAvailable,
            Boolean isCompletedCreation, UUID userUuid,
            Pageable pageable
    ) {

        Specification<Course> courseSpecification = Specification.where(
                CourseSpecification.isAvailableSpecification(isAvailable)
                        .and(CourseSpecification.sectionSpecification(section))
                        .and(CourseSpecification.isCompletedCreationSpecification(isCompletedCreation))
                        .and(CourseSpecification.userIdSpecification(userUuid))
        );

        Page<Course> result = courseRepository.findAll(courseSpecification, pageable);

        //Page<Course> result = courseRepository.findAllBySections_Id(section, pageable);

        return result.map(
                course -> {
                    AdminCourseCreationResponse response = courseMapper.toAdminCourseCreationResponse(course);

                    int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
                    response.setLessonCount(lessonCount);

                    List<Section> sections = course.getSections();
                    response.setSections(sections);

                    int numberOfEnrolledStudents = course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0;
                    response.setNumberOfEnrolledStudents(numberOfEnrolledStudents);

                    return response;
                }
        );
    }

    public List<Object[]> getCompletionRate(String type, LocalDate startDate, LocalDate endDate) {
        String unit = switch (type) {
            case "hourly" -> "hour";
            case "daily" -> "day";
            case "weekly" -> "week";
            case "monthly" -> "month";
            case "custom" -> "day";
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().plusDays(1);

        return userCoursesRepository.getCompletionRateByRange(unit, start.atStartOfDay(ZoneId.systemDefault()).toInstant(), end.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Page<CourseCreationResponse> getAllCoursesExceptEnrolledByUser(UUID userId, Pageable pageable) {

        Page<Course> courses;
        if (userId == null) {

            Specification<Course> specification = Specification.where(
                    CourseSpecification.isAvailableSpecification(true)
                            .and(CourseSpecification.isCompletedCreationSpecification(true))
            );

            courses = courseRepository.findAll(specification, pageable);
        } else {
            // default get available course
            courses = courseRepository.findAllCoursesExceptEnrolledByUser(userId, pageable);
        }
        return courses.map(course -> {
            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
            CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
            response.setLessonCount(lessonCount);

            List<Section> sections = course.getSections();
            response.setSections(sections);
            return response;
        });
    }

    @Transactional
    public void deleteCourseById(UUID courseId, String userUid) {
        UUID userId = ParseUUID.normalizeUID(userUid);
        Course course = courseRepository.findByCourseIdAndUserId(courseId, userId);

        if (course == null) {
            throw new AppException(ErrorCode.COURSE_NOT_EXISTED);
        }

        // Check if the course is already enrolled by any user
        boolean existedUserEnrolled = userCoursesRepository.existsByEnrollId_CourseId(courseId);
        if (existedUserEnrolled) {
            throw new AppException(ErrorCode.COURSE_ALREADY_ENROLLED_CANNOT_DELETE);
        }

        log.info("Deleting course with ID: {}", courseId);

        if (course.getCourseImage() != null) {
            cloudinaryService.deleteImage(course.getCourseImage());
        }

        try {
            courseRepository.delete(course);
            //deleteCourseVectorEmbedding(courseId);
            aiServiceClient.deleteCourseEmbeddingData(courseId)
                    .doOnSuccess(response -> log.info("Successfully deleted embedding data for course {}", courseId))
                    .doOnError(error -> log.error("Error deleting embedding data for course {}: {}", courseId, error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        /*course.setTopic(null);
        course = courseRepository.save(course);
        courseRepository.flush();

        courseRepository.delete(course);*/
    }

    public CourseCreationResponse createCourse(UUID userUid, CourseCreationRequest request) {
        Course course = courseMapper.toCourse(request);

        course.setUserId(userUid);
        course.setLessons(new ArrayList<>());
        course.setReviews(new ArrayList<>());
        course.setEnrollCourses(new ArrayList<>());
        course.setAverageRating(0.0);
        course.setReviewCount(0);


        course.setTopic(null);

        course = courseRepository.save(course);

        return courseMapper.toCourseCreationResponse(course);
    }

    public CourseCreationResponse updateCourse(UUID courseId, CourseUpdateRequest request, String userUid) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        if (course.getUserId().equals(ParseUUID.normalizeUID(userUid))) {
            throw new AppException(ErrorCode.INVALID_USER);
        }

        courseMapper.updateCourse(course, request);
        course.setUserId(ParseUUID.normalizeUID(request.getUserUid()));

        /*List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        course.setLessons(lessons);*/

        course = courseRepository.save(course);
        return courseMapper.toCourseCreationResponse(course);
    }

    public Page<CourseSearchResponse> searchCoursesWithFilter(UUID userid,
                                                              String keyword,
                                                              Float rating,
                                                              List<String> levels,
                                                              Float priceFrom,
                                                              Float priceTo,
                                                              List<Integer> categories,
                                                              Pageable pageable) {
        if (pageable.getSort().isUnsorted() && rating != null) {
            // set pageable sort by rating asc if rating is not null
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "averageRating"));
        }
        Specification<Course> specification = Specification.where(
                (CourseSpecification.nameSpecification(keyword).or(CourseSpecification.descriptionSpecification(keyword))
                        .and(CourseSpecification.ratingSpecification(rating))
                        .and(CourseSpecification.levelsSpecification(levels))
                        .and(CourseSpecification.priceRangeSpecification(priceFrom, priceTo))
                        .and(CourseSpecification.categoriesSpecification(categories)
                                .and(CourseSpecification.isAvailableSpecification(true))
                                .and(CourseSpecification.isCompletedCreationSpecification(true))
                        )
                ));

        Page<Course> result = courseRepository.findAll(specification, pageable);

        return getCourseSearchResponses(userid, result);
    }

    public Page<AdminCourseSearchResponse> searchCoursesOfAdminWithFilter(UUID userUid,
                                                                          String keyword,
                                                                          Float rating,
                                                                          List<String> levels,
                                                                          Float priceFrom,
                                                                          Float priceTo,
                                                                          List<Integer> categories,
                                                                          Boolean isAvailable,
                                                                          Boolean isCompletedCreation,
                                                                          Pageable pageable) {
        Specification<Course> specification = Specification.where(
                (CourseSpecification.nameSpecification(keyword).or(CourseSpecification.descriptionSpecification(keyword))
                        .and(CourseSpecification.ratingSpecification(rating))
                        .and(CourseSpecification.levelsSpecification(levels))
                        .and(CourseSpecification.priceRangeSpecification(priceFrom, priceTo))
                        .and(CourseSpecification.categoriesSpecification(categories)
                                .and(CourseSpecification.isAvailableSpecification(isAvailable))
                                .and(CourseSpecification.isCompletedCreationSpecification(isCompletedCreation))
                                .and(CourseSpecification.userIdSpecification(userUid))
                        )
                ));

        Page<Course> result = courseRepository.findAll(specification, pageable);

        return result.map(course -> {
            AdminCourseSearchResponse response = courseMapper.toAdminCourseSearchResponse(course);

            List<Section> sections = course.getSections();
            response.setSections(sections);

            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
            response.setLessonCount(lessonCount);

            int numberOfEnrolledStudents = course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0;
            response.setNumberOfEnrolledStudents(numberOfEnrolledStudents);

            return response;
        });

        //return getCourseSearchResponsesOfAdmin(result);
    }

    @NotNull
    private Page<CourseSearchResponse> getCourseSearchResponses(UUID userId, Page<Course> result) {
        return result.map(course -> {
            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;  //lessonRepository.countByCourse_CourseId(course.getCourseId());
            CourseSearchResponse response = courseMapper.toCourseSearchResponse(course);
            response.setLessonCount(lessonCount);

            List<Section> sections = course.getSections();
            response.setSections(sections);

            // Check if the user is enrolled in the course
            response.setCertificateId(null);
            response.setCertificateUrl(null);

            if (userId == null) {
                return response;
            }

            // Check if the user is enrolled in the course
            boolean isUserEnrolled = userCoursesRepository.existsByEnrollId_UserUidAndEnrollId_CourseIdAndAccessStatus(
                    userId, course.getCourseId(), UserCourseAccessStatus.ACCESSIBLE.getCode()
            );

            String certificateUrl = null;
            String certificateId = null;

            if (isUserEnrolled) {
                UserCourses userCourse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userId, course.getCourseId())
                        .orElse(null);

                certificateId = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateId().toString() : null;
                certificateUrl = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateUrl() : null;
            }

            response.setCertificateUrl(certificateUrl);
            response.setCertificateId(certificateId);
            return response;
        });
    }

    @NotNull
    private Page<AdminCourseSearchResponse> getCourseSearchResponsesOfAdmin(Page<Course> result) {
        return result.map(course -> {
            AdminCourseSearchResponse response = courseMapper.toAdminCourseSearchResponse(course);

            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;  //lessonRepository.countByCourse_CourseId(course.getCourseId());
            response.setLessonCount(lessonCount);

            List<Section> sections = course.getSections();
            response.setSections(sections);

            int numberOfEnrolledStudents = course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0;
            response.setNumberOfEnrolledStudents(numberOfEnrolledStudents);

            return response;
        });
    }

    public Page<CourseSearchResponse> searchCourses(
            UUID userUid, String keyword,
            Pageable pageable
    ) {

        Specification<Course> specification = Specification.where(
                CourseSpecification.isAvailableSpecification(true)
                        .and(CourseSpecification.courseNameOrDescriptionSpecification(keyword, keyword))
                        .and(CourseSpecification.isCompletedCreationSpecification(true))
        );

        Page<Course> courses = courseRepository.findAll(specification, pageable);

        //Page<Course>  courses = courseRepository.findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        return getCourseSearchResponses(userUid, courses);
    }

    public Page<AdminCourseSearchResponse> searchCoursesOfAdmin(
            UUID userUid, String keyword,
            Boolean isAvailable, Boolean isCompletedCreation,
            Pageable pageable
    ) {

        Specification<Course> specification = Specification.where(
                CourseSpecification.isAvailableSpecification(isAvailable)
                        .and(CourseSpecification.courseNameOrDescriptionSpecification(keyword, keyword))
                        .and(CourseSpecification.isCompletedCreationSpecification(isCompletedCreation))
                        .and(CourseSpecification.userIdSpecification(userUid))
        );

        Page<Course> courses = courseRepository.findAll(specification, pageable);

        //Page<Course>  courses = courseRepository.findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);

        return courses.map(course -> {
            AdminCourseSearchResponse response = courseMapper.toAdminCourseSearchResponse(course);

            List<Section> sections = course.getSections();
            response.setSections(sections);

            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0; //lessonRepository.countByCourse_CourseId(course.getCourseId());
            response.setLessonCount(lessonCount);

            int numberOfEnrolledStudents = course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0;
            response.setNumberOfEnrolledStudents(numberOfEnrolledStudents);

            return response;
        });

        //return getCourseSearchResponsesOfAdmin(courses);
    }

    private String mapCertificateTemplateEnums(Integer templateId) {
        if (templateId == null) {
            return null;
        }

        return switch (templateId) {
            case 1 -> CertificateTemplate1.linkExample;
            case 2 -> CertificateTemplate2.linkExample;
            default -> null;
        };
    }


    public DetailCourseResponse getCourseById(UUID courseId, UUID userUid) {
        // Fetch the course by ID or throw an exception if it doesn't exist
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Count the number of lessons in the course
        //int lessonCount = lessonRepository.countByCourse_CourseId(courseId);
        int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;

        // Calculate the average rating for the course
        /*double averageRating = course.getReviews().stream()
                .mapToDouble(review -> Optional.of(review.getRating()).orElse(0))
                .average()
                .orElse(0.0);*/

        // Count the number of reviews for the course
        //int reviewCount = course.getReviews().size();

        // Check if the user is enrolled in the course
        boolean isUserEnrolled = userCoursesRepository.existsByEnrollId_UserUidAndEnrollId_CourseIdAndAccessStatus(
                userUid, courseId, UserCourseAccessStatus.ACCESSIBLE.getCode()
        );

        // Get the latest lesson ID for the user (if enrolled)
        UUID latestLessonId = null;
        if (isUserEnrolled) {
            UserCourses userCourse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                    .orElse(null);
            latestLessonId = (userCourse != null) ? userCourse.getLatestLessonId() : null;
        }

        // Calculate the completion ratio for the user
        float completionRatio = 0.0f;
        if (isUserEnrolled) {

            /*Specification<Lesson> hasExerciseSpecification = Specification.where(
                    LessonSpecification.hasCourseId(courseId)
            ).and(
                    LessonSpecification.hasExerciseId()
            );*/

            Specification<Lesson> hasProblemSpecification = Specification.where(
                    LessonSpecification.hasCourseId(courseId)
            ).and(
                    LessonSpecification.hasProblemId()
            );

            int totalLessonsHasExercise = lessonRepository.countByCourse_CourseId(courseId);
            int totalLessonsHasProblem = (int) lessonRepository.count(hasProblemSpecification);

            log.info("Total lessons has exercise: {}", totalLessonsHasExercise);
            log.info("Total lessons has problem: {}", totalLessonsHasProblem);

            int completedLessons = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDoneTheory(userUid, courseId, true);
            int completedPractices = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDonePractice(userUid, courseId, true);
            completedLessons += completedPractices;

            log.info("Completed lessons: {}", completedLessons);

            if (totalLessonsHasProblem + totalLessonsHasExercise > 0) {
                completionRatio = (completedLessons / (float) (totalLessonsHasExercise + totalLessonsHasProblem)) * 100;
            }
        }

        String certificateUrl = null;
        String certificateId = null;

        if (isUserEnrolled) {
            UserCourses userCourse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                    .orElse(null);

            certificateId = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateId().toString() : null;
            certificateUrl = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateUrl() : null;
        }

        // Map course details to the response DTO
        return DetailCourseResponse.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .level(course.getLevel())
                .price(course.getPrice())
                .unitPrice(course.getUnitPrice())
                .userUid(course.getUserId())
                .lessonCount(lessonCount)
                .averageRating(course.getAverageRating())
                .reviewCount(course.getReviewCount())
                .isUserEnrolled(isUserEnrolled)
                .latestLessonId(latestLessonId)
                .progressPercent(completionRatio)
                .certificateUrl(certificateUrl)
                .certificateId(certificateId)
                .courseImage(course.getCourseImage())
                .templateCode(course.getTemplateCode())
                .createdAt(course.getCreatedAt())
                .numberOfEnrolledStudents(course.getEnrollCourses() != null ? course.getEnrollCourses().size() : 0)
                .aiSummaryContent(course.getCourseSummary() != null && course.getCourseSummary().getSummaryContent() != null ? course.getCourseSummary().getSummaryContent() : null)
                .templateLink(course.getTemplateCode() != null ? mapCertificateTemplateEnums(course.getTemplateCode()) : null)
                .categories(course.getCategories() != null ? course.getCategories().stream()
                        .map(categoryMapper::categoryToCategoryResponse)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public DetailCourseResponse getDetailsCourseById(UUID courseId, UUID userUid) {
        // Fetch the course by ID or throw an exception if it doesn't exist
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Count the number of lessons in the course
        //int lessonCount = lessonRepository.countByCourse_CourseId(courseId);
        int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;

        // Calculate the average rating for the course
        /*double averageRating = course.getReviews().stream()
                .mapToDouble(review -> Optional.of(review.getRating()).orElse(0))
                .average()
                .orElse(0.0);*/

        // Count the number of reviews for the course
        //int reviewCount = course.getReviews().size();

        // Check if the user is enrolled in the course
        boolean isUserEnrolled = userCoursesRepository.existsByEnrollId_UserUidAndEnrollId_CourseIdAndAccessStatus(
                userUid, courseId, UserCourseAccessStatus.ACCESSIBLE.getCode()
        );

        // Get the latest lesson ID for the user (if enrolled)
        UUID latestLessonId = null;
        if (isUserEnrolled) {
            UserCourses userCourse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                    .orElse(null);
            latestLessonId = (userCourse != null) ? userCourse.getLatestLessonId() : null;
        }

        // Calculate the completion ratio for the user
        float completionRatio = 0.0f;
        if (isUserEnrolled) {

            /*Specification<Lesson> hasExerciseSpecification = Specification.where(
                    LessonSpecification.hasCourseId(courseId)
            ).and(
                    LessonSpecification.hasExerciseId()
            );*/

            Specification<Lesson> hasProblemSpecification = Specification.where(
                    LessonSpecification.hasCourseId(courseId)
            ).and(
                    LessonSpecification.hasProblemId()
            );

            int totalLessonsHasExercise = lessonRepository.countByCourse_CourseId(courseId);
            int totalLessonsHasProblem = (int) lessonRepository.count(hasProblemSpecification);

            log.info("Total lessons has exercise: {}", totalLessonsHasExercise);
            log.info("Total lessons has problem: {}", totalLessonsHasProblem);

            int completedLessons = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDoneTheory(userUid, courseId, true);
            int completedPractices = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDonePractice(userUid, courseId, true);
            completedLessons += completedPractices;

            log.info("Completed lessons: {}", completedLessons);

            if (totalLessonsHasProblem + totalLessonsHasExercise > 0) {
                completionRatio = (completedLessons / (float) (totalLessonsHasExercise + totalLessonsHasProblem)) * 100;
            }
        }

        String certificateUrl = null;
        String certificateId = null;

        if (isUserEnrolled) {
            UserCourses userCourse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                    .orElse(null);

            certificateId = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateId().toString() : null;
            certificateUrl = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateUrl() : null;
        }

        // Map course details to the response DTO
        return DetailCourseResponse.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .level(course.getLevel())
                .price(course.getPrice())
                .unitPrice(course.getUnitPrice())
                .userUid(course.getUserId())
                .lessonCount(lessonCount)
                .averageRating(course.getAverageRating())
                .reviewCount(course.getReviewCount())
                .isUserEnrolled(isUserEnrolled)
                .latestLessonId(latestLessonId)
                .progressPercent(completionRatio)
                .certificateUrl(certificateUrl)
                .certificateId(certificateId)
                .courseImage(course.getCourseImage())
                .templateCode(course.getTemplateCode())
                .createdAt(course.getCreatedAt())
                .numberOfEnrolledStudents(course.getEnrollCourses() != null
                        ? course.getEnrollCourses().size()
                        : 0)
                .aiSummaryContent(course.getCourseSummary() != null
                        && course.getCourseSummary().getSummaryContent() != null
                        ? course.getCourseSummary().getSummaryContent()
                        : null)
                .templateLink(course.getTemplateCode() != null
                        ? mapCertificateTemplateEnums(course.getTemplateCode())
                        : null)
                .categories(course.getCategories() != null
                        ? course.getCategories().stream()
                        .map(categoryMapper::categoryToCategoryResponse)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    public AdminCourseCreationResponse getCourseOfAdminByCourseId(UUID courseId, UUID userUid, String userRole) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        // Fetch the course by ID or throw an exception if it doesn't exist
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (!course.getUserId().equals(userUid)) {
            throw new AppException(ErrorCode.USER_NOT_OWN_COURSE);
        }

        AdminCourseCreationResponse response = courseMapper.toAdminCourseCreationResponse(course);

       /* if (course.getCourseSummary() != null && course.getCourseSummary().getSummaryContent()!= null) {
            CourseSummary courseSummary = course.getCourseSummary();
            response.setAiSummaryContent(courseSummary.getSummaryContent());
        }*/


        return response;
    }


    public List<DetailCourseResponse> getDetailsOfCourses(List<UUID> courseIds, UUID userUid) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Fetch course details for all provided course IDs
        List<DetailCourseResponse> courseDetails = new ArrayList<>();
        for (UUID courseId : courseIds) {
            try {
                DetailCourseResponse detailCourse = getCourseById(courseId, userUid);
                courseDetails.add(detailCourse);
            } catch (AppException ex) {
                log.warn("Error fetching details for course ID {}: {}", courseId, ex.getMessage());
                // Optionally, you can choose to skip or include null responses for invalid IDs.
            }
        }

        return courseDetails;
    }


    public UserCourses enrollCourse(UUID userUid, UUID courseId, String subscriptionPlan) {
        if (userUid == null || courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Lấy đối tượng Course từ cơ sở dữ liệu
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));


        if (course.getPrice() > 0) {
            // check if user has subscription plan
            if (subscriptionPlan != null &&
                    (subscriptionPlan.equals(PremiumPackage.COURSE_PLAN.getCode())
                            || subscriptionPlan.equals(PremiumPackage.PREMIUM_PLAN.getCode())
                    )
            ) {
                // enroll course using subscription plan
                return enrollPaidCourse(userUid, courseId, true);

            }

            // if user does not have subscription plan, return error
            throw new AppException(ErrorCode.COURSE_NOT_FREE);
        }

        // Kiểm tra nếu đã có đăng ký khóa học này
        UserCourses userCoursesResponse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                .orElseGet(() -> {
                    // Tạo đối tượng UserCourses mới
                    UserCourses newUserCourses = UserCourses.builder()
                            .enrollId(EnrollCourse.builder()
                                    .userUid(userUid)
                                    .courseId(courseId)
                                    .build())
                            .course(course)  // Đảm bảo course không null
                            .status(PredefinedLearningStatus.LEARNING)
                            .progressPercent(0.0f)
                            .enrollUsingSubscription(false)
                            .accessStatus(UserCourseAccessStatus.ACCESSIBLE.getCode())
                            .build();

                    // create default learning lesson progress for user
                    lessonRepository.findAllByCourse_CourseIdOrderByLessonOrderDesc(courseId).forEach(lesson -> {
                        LearningLesson learningLesson = LearningLesson.builder()
                                .lesson(lesson)
                                .userId(userUid)
                                .status(PredefinedLearningStatus.NEW)
                                .assignments(new ArrayList<>())
                                .isDoneTheory(null)
                                .isDonePractice(false)
                                .build();
                        learningLessonRepository.save(learningLesson);
                    });


                    // Lưu đối tượng UserCourses mới vào cơ sở dữ liệu
                    return userCoursesRepository.save(newUserCourses);
                });

        if (userCoursesResponse.getEnrollUsingSubscription()
                || !userCoursesResponse.getAccessStatus().equals(
                UserCourseAccessStatus.ACCESSIBLE.getCode())
        ) {
            userCoursesResponse.setAccessStatus(
                    UserCourseAccessStatus.ACCESSIBLE.getCode()
            );
            userCoursesResponse.setEnrollUsingSubscription(false);

            userCoursesResponse = userCoursesRepository.save(userCoursesResponse);
        }

        return userCoursesResponse;
    }

    public UserCourses enrollPaidCourse(
            UUID userUid, UUID courseId,
            Boolean enrollUsingSubscription
    ) {
        if (userUid == null || courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Lấy đối tượng Course từ cơ sở dữ liệu
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Kiểm tra nếu đã có đăng ký khóa học này
        UserCourses userCoursesResponse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                .orElseGet(() -> {
                    // Tạo đối tượng UserCourses mới
                    UserCourses newUserCourses = UserCourses.builder()
                            .enrollId(EnrollCourse.builder()
                                    .userUid(userUid)
                                    .courseId(courseId)
                                    .build())
                            .course(course)  // Đảm bảo course không null
                            .status(PredefinedLearningStatus.LEARNING)
                            .progressPercent(0.0f)
                            .enrollUsingSubscription(enrollUsingSubscription)
                            .accessStatus(UserCourseAccessStatus.ACCESSIBLE.getCode())
                            .build();

                    // create default learning lesson progress for user
                    lessonRepository.findAllByCourse_CourseIdOrderByLessonOrderDesc(courseId).forEach(lesson -> {
                        LearningLesson learningLesson = LearningLesson.builder()
                                .lesson(lesson)
                                .userId(userUid)
                                .status(PredefinedLearningStatus.NEW)
                                .assignments(new ArrayList<>())
                                .isDoneTheory(null)
                                .isDonePractice(false)
                                .build();
                        learningLessonRepository.save(learningLesson);
                    });

                    // Lưu đối tượng UserCourses mới vào cơ sở dữ liệu
                    return userCoursesRepository.save(newUserCourses);
                });

        if (!userCoursesResponse.getAccessStatus().equals(
                UserCourseAccessStatus.ACCESSIBLE.getCode()
        )) {
            userCoursesResponse.setAccessStatus(
                    UserCourseAccessStatus.ACCESSIBLE.getCode()
            );

            userCoursesResponse = userCoursesRepository.save(userCoursesResponse);
        }

        return userCoursesResponse;
    }

    public List<UserCourses> reEnrollPaidCourseWhenResubscribePlan(UUID userUid, String subscriptionPlan) {
        if (userUid == null || subscriptionPlan == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (!subscriptionPlan.equals(PremiumPackage.COURSE_PLAN.getCode())
                && !subscriptionPlan.equals(PremiumPackage.PREMIUM_PLAN.getCode())
        ) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Specification<UserCourses> specification = Specification.where(
                UserCoursesSpecification.hasUserUid(userUid)
                        .and(UserCoursesSpecification.isEnrollUsingSubscription(true))
                        .and(UserCoursesSpecification.hasAccessStatus(UserCourseAccessStatus.INACCESSIBLE.getCode()))
        );

        List<UserCourses> userCoursesResponse = userCoursesRepository.findAll(specification);

        userCoursesResponse.forEach(userCourse -> {
            userCourse.setAccessStatus(UserCourseAccessStatus.ACCESSIBLE.getCode());
            userCoursesRepository.save(userCourse);
        });

        return userCoursesResponse;
    }

    public Boolean disenrollCourse(UUID userUid, UUID courseId) {
        if (userUid == null || courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_COURSE_NOT_EXISTED));

        List<LearningLesson> learningLessons = learningLessonRepository.findAllByUserIdAndLesson_Course_CourseId(userUid, courseId);
        learningLessonRepository.deleteAll(learningLessons);
        userCoursesRepository.delete(userCourses);
        return true;
    }

    public Boolean disenrollCoursesEnrolledUsingSubscriptionPlan(List<UUID> userUuids) {
        if (userUuids == null || userUuids.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        log.info("Disenroll users: {}", userUuids);

        userUuids.forEach(userUid -> {
            Specification<UserCourses> specification = Specification.where(
                    (UserCoursesSpecification.hasUserUid(userUid))
                            .and(UserCoursesSpecification.isEnrollUsingSubscription(true))
                            .and(UserCoursesSpecification.hasAccessStatus(UserCourseAccessStatus.ACCESSIBLE.getCode()))
            );
            List<UserCourses> userCourses = userCoursesRepository.findAll(specification);

            userCourses.forEach(userCourse -> {
                userCourse.setAccessStatus(UserCourseAccessStatus.INACCESSIBLE.getCode());
                userCoursesRepository.save(userCourse);
            });

        });

        return true;
    }

    public List<EnrolledCourseResponse> getEnrolledUsersOfCourse(UUID courseId) {
        if (courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // reduce data size response to fe
        course.setReviews(new ArrayList<>());
        course.setLessons(new ArrayList<>());
        course.setEnrollCourses(new ArrayList<>());

        List<UserCourses> listEnrolledUserInCourse = userCoursesRepository.findAllByEnrollId_CourseId(courseId);
        List<EnrolledCourseResponse> listEnrolledUsersResponse = new ArrayList<>();
        for (UserCourses userCourses : listEnrolledUserInCourse) {
            userCourses.setCourse(course);
            listEnrolledUsersResponse.add(EnrolledCourseResponse.builder()
                    .course(course)
                    .enrollId(userCourses.getEnrollId())
                    .lastAccessedDate(userCourses.getLastAccessedDate())
                    .progressPercent(userCourses.getProgressPercent())
                    .status(userCourses.getStatus())
                    .build());
        }
        return listEnrolledUsersResponse;
    }

    public List<CompleteCourseResponse> getCompleteCourseByUserId(UUID userUid) {
        List<UserCourses> listEnrolledUserInCourse = userCoursesRepository.findAllByEnrollId_UserUid(userUid);
        List<CompleteCourseResponse> listEnrolledUsersResponse = new ArrayList<>();
        for (UserCourses userCourses : listEnrolledUserInCourse) {
            if (userCourses.getStatus().equals("Done")) {
                listEnrolledUsersResponse.add(CompleteCourseResponse.builder()
                        .course(userCourses.getCourse())
                        .enrollId(userCourses.getEnrollId())
                        .lastAccessedDate(userCourses.getLastAccessedDate())
                        .progressPercent(userCourses.getProgressPercent())
                        .status(userCourses.getStatus())
                        .certificateId(userCourses.getCertificate() != null ?
                                userCourses.getCertificate().getCertificateId()
                                : null)
                        .completedDate(userCourses.getCertificate() != null ? (
                                        Date.from(userCourses.getCertificate().getCompletedDate())
                                ) : null
                        )
                        .build());
            }
        }
        return listEnrolledUsersResponse;
    }

    public Page<DetailCourseResponse> getEnrolledCoursesOfUser(UUID userUuid, Pageable pageable) {
        if (userUuid == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        /*int count = courseRepository.countAllEnrolledByUser(userUuid);
        log.info("Count of enrolled courses: {}", count);*/

        Page<Course> courses = courseRepository.findAllCoursesEnrolledByUser(userUuid, pageable);

        return courses.map(course -> {

            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;
            UUID latestLessonId = null;
            UserCourses userCourse = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUuid, course.getCourseId())
                    .orElse(null);
            latestLessonId = (userCourse != null) ? userCourse.getLatestLessonId() : null;
            float completionRatio = 0.0f;

            Specification<Lesson> hasProblemSpecification = Specification.where(
                    LessonSpecification.hasCourseId(course.getCourseId())
            ).and(
                    LessonSpecification.hasProblemId()
            );

            int totalLessonsHasExercise = lessonRepository.countByCourse_CourseId(course.getCourseId());
            int totalLessonsHasProblem = (int) lessonRepository.count(hasProblemSpecification);

            log.info("Total lessons has exercise: {}", totalLessonsHasExercise);
            log.info("Total lessons has problem: {}", totalLessonsHasProblem);

            int completedLessons = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDoneTheory(userUuid, course.getCourseId(), true);
            int completedPractices = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDonePractice(userUuid, course.getCourseId(), true);
            completedLessons += completedPractices;

            log.info("Completed lessons: {}", completedLessons);

            if (totalLessonsHasProblem + totalLessonsHasExercise > 0) {
                completionRatio = (completedLessons / (float) (totalLessonsHasExercise + totalLessonsHasProblem)) * 100;
            }

            String certificateId = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateId().toString() : null;
            String certificateUrl = (userCourse != null && userCourse.getCertificate() != null) ? userCourse.getCertificate().getCertificateUrl() : null;

            return DetailCourseResponse.builder()
                    .courseId(course.getCourseId())
                    .courseName(course.getCourseName())
                    .description(course.getDescription())
                    .level(course.getLevel())
                    .price(course.getPrice())
                    .unitPrice(course.getUnitPrice())
                    .userUid(course.getUserId())
                    .lessonCount(lessonCount)
                    .averageRating(course.getAverageRating())
                    .reviewCount(course.getReviewCount())
                    .isUserEnrolled(true) // Always true since this is for enrolled courses
                    .latestLessonId(latestLessonId)
                    .progressPercent(completionRatio)
                    .certificateUrl(certificateUrl)
                    .certificateId(certificateId)
                    .courseImage(course.getCourseImage())
                    .templateCode(course.getTemplateCode())
                    .createdAt(course.getCreatedAt())
                    .aiSummaryContent(course.getCourseSummary() != null
                            && course.getCourseSummary().getSummaryContent() != null
                            ? course.getCourseSummary().getSummaryContent()
                            : null)
                    .templateLink(course.getTemplateCode() != null
                            ? mapCertificateTemplateEnums(course.getTemplateCode())
                            : null)
                    .categories(null)
                    .build();
        });

        /*Specification<UserCourses> specification = Specification.where(
                UserCoursesSpecification.hasUserUid(userUuid)
                        .and(UserCoursesSpecification.hasAccessStatus(UserCourseAccessStatus.ACCESSIBLE.getCode()))
        );

        return userCoursesRepository.findAll(specification, pageable);*/
        //return userCoursesRepository.findAllByEnrollId_UserUid(userUuid, pageable);
        /*return userCourses.map(userCourse -> {
            DetailCourseResponse detailCourseResponse = detailsCourseRepositoryCustom
                    .getDetailsCourse(userCourse.getEnrollId().getCourseId(), userUuid)
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
            userCourse.setProgressPercent(detailCourseResponse.getProgressPercent());
            return userCourse;
        });*/
    }

    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::categoryToCategoryResponse).collect(Collectors.toList());
    }

    public List<CategoryResponse> getListCategory(List<Integer> ids) {
        List<Category> categories = new ArrayList<>();
        categories = categoryRepository.findAllById(ids);
        return categories.stream().map(categoryMapper::categoryToCategoryResponse).collect(Collectors.toList());
    }

    public CategoryResponse getCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return categoryMapper.categoryToCategoryResponse(category);
    }

    public String getUserNameFromToken(String token) {
        if (token == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        ResponseEntity<ValidatedTokenResponse> tokenResponse = identityClient.validateToken(token).block();

        assert tokenResponse != null;
        if (!tokenResponse.getBody().isValidated())
            throw new AppException(ErrorCode.BAD_REQUEST);

        return tokenResponse.getBody().getName();
    }

    private static String getDaySuffix(int day) {
        return (day >= 11 && day <= 13) ? "th" : new String[]{"th", "st", "nd", "rd"}[(day % 10 < 4) ? day % 10 : 0];
    }

    @ExecutionTiming
    public CertificateCreationResponse createCertificate(UUID courseId, UUID userId) throws Exception {
        //long startTime = System.currentTimeMillis();

        System.out.println("userid: " + userId + "\n" +
                "courseId: " + courseId);

        UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userId, courseId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_COURSE_NOT_EXISTED));

        Instant instant = Instant.now();

        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault()); // Lấy múi giờ hệ thống

        String completedDate = String.format("%d%s %s",
                zonedDateTime.getDayOfMonth(),
                getDaySuffix(zonedDateTime.getDayOfMonth()),
                zonedDateTime.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        );


        User user = firestoreService.getUserById(userCourses.getEnrollId().getUserUid().toString());
        if (user == null) {
            log.error("Error while get user by user id: " + userCourses.getEnrollId().getUserUid());
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        String userName = user.getFirstName()
                + " " + user.getLastName();

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        String courseName = course.getCourseName();


//        String directorName = "Phạm Nguyễn Sơn Tùng";
//        Image sign = null;
        try {
            byte[] certificateImage;
            if (course.getTemplateCode() == 2) {
                certificateImage = CertificateTemplate2.createCertificate(completedDate, userName, courseName);
            } else {
                certificateImage = CertificateTemplate1.createCertificate(completedDate, userName, courseName);
            }

            String fileName = courseId + "-" + userId.toString();
            Map upload = CertificateTemplate1.uploadCertificateImage(certificateImage, fileName);

            String url = (String) upload.get("secure_url");
            Certificate newCertificate = new Certificate();

            newCertificate.setCertificateUrl(url);
            newCertificate.setCompletedDate(instant);

            Certificate certificate = certificateRepository.save(newCertificate);

            userCourses.setCertificate(certificate);
            userCoursesRepository.save(userCourses);


            CertificateCreationResponse certificateCreationResponse = new CertificateCreationResponse();

            certificateCreationResponse.setUrl(certificate.getCertificateUrl());
            certificateCreationResponse.setCourseId(courseId);
            certificateCreationResponse.setUserId(userId);

            try {
                notificationService.uploadLeaderBoard(userId, course);
            } catch (Exception e) {
                log.error("Error while uploading leader board: " + e.getMessage());
            }
            try {
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTitle("Congratulations on Completing Your Course!");
                notificationRequest.setMessage("Dear [@" + userName + "],\n" +
                        "\n" +
                        "Congratulations on successfully completing the \"" + courseName + "\" on [Platform/Institution Name]! We recognize your dedication and commitment to learning.\n" +
                        "\n" +
                        "\uD83D\uDCDC Your Certificate of Completion is Ready! You can download it here: [Certificate Download Link]\n" +
                        "\n" +
                        "\uD83D\uDD39 Certificate Details:\n" +
                        "\n" +
                        "Learner’s Name: [@" + userName + "]\n" +
                        "Course Name: " + courseName + "\n" +
                        "Completion Date: [Completion Date]\n" +
                        "Issued by: Intellab\n" +
                        "Showcase your achievement by sharing your certificate on LinkedIn or other social media platforms!\n" +
                        "\n" +
                        "Thank you for learning with us. We wish you continued success in your future endeavors!\n" +
                        "\n" +
                        "\n");
                notificationRequest.setUserid(userId);
                notificationRequest.setRedirectType("COURSE_COMPLETE");
                notificationRequest.setRedirectContent("/certificate/" + certificate.getCertificateId());
                identityClient.postNotifications(notificationRequest).block().getResult().getMessage();
            } catch (Exception e) {
                System.out.println("Error in creating notification: " + e);
            }
            return certificateCreationResponse;

        } catch (Exception ex) {
            String msg = "Error creating certificate";
            log.error(msg, ex);
            throw ex;
        }
        /*finally {
            long endTime = System.currentTimeMillis(); // End time measurement
            long duration = endTime - startTime; // Calculate duration
            log.info("createCertificate execution time: {} ms", duration); // Log the duration
        }*/
    }

    public CertificateResponse getCertificate(UUID certificateId) throws ExecutionException, InterruptedException {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new AppException(ErrorCode.CERTIFICATE_NOT_FOUND));
        CertificateResponse certificateResponse = new CertificateResponse();
        UserCourses userCourses = certificate.getUserCourses();

        Course course = userCourses.getCourse();
        CourseShortResponse courseShortResponse = courseMapper.toCourseShortResponse(course);
        courseShortResponse.setReviewCount(course.getReviews().size());
        certificateResponse.setCourse(courseShortResponse);

        certificateResponse.setCertificateLink(certificate.getCertificateUrl());
        certificateResponse.setCompleteDate(certificate.getCompletedDate());
        User user = firestoreService.getUserById(userCourses.getEnrollId().getUserUid().toString());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        String username = user.getLastName()
                + " " + user.getFirstName();

        certificateResponse.setUserUid(user.getUid());

        certificateResponse.setUsername(username);

        return certificateResponse;
    }

    public String GetCertificateTemplateExample(Integer templateId) {
        if (templateId == 1) {
            return CertificateTemplate1.linkExample;
        } else if (templateId == 2) {
            return CertificateTemplate2.linkExample;
        }
        return null;
    }

    private AuthorCourseResponse toAuthorCourseResponse(Course course) {
        return AuthorCourseResponse.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .level(course.getLevel())
                .price(course.getPrice())
                .unitPrice(course.getUnitPrice())
                .userId(course.getUserId())
                .averageRating(course.getAverageRating())
                .reviewCount(course.getReviews().size())
                .lessonCount(course.getLessons().size())
                .lessons(course.getLessons().stream().map(lessonMapper::toLessonResponse).collect(Collectors.toList()))
                .categories(course.getCategories().stream().map(categoryMapper::categoryToCategoryResponse).collect(Collectors.toList()))
                .sections(course.getSections())
                .build();
    }

    public Page<AuthorCourseResponse> getAuthorCourses(Pageable pageable, UUID userId) {
        Page<Course> courses = courseRepository.findByUserId(pageable, userId);
        return courses.map(this::toAuthorCourseResponse);
    }

    public Boolean hasUserAlreadyEnrollCourse(CheckingUserCourseExistedRequest request) {
        log.info("Checking user course existed: {}", request.getUserUuid() + " " + request.getProblemId());
        return userCoursesRepository.existsByProblemIdAndUserIdAAndAccessStatus(
                request.getProblemId(),
                request.getUserUuid(),
                UserCourseAccessStatus.ACCESSIBLE.getCode()
        );
    }

    public CourseAndFirstLessonResponse getCourseAndFirstLessonByCourseId(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        List<Lesson> lessons = lessonRepository.findAllByCourse_CourseId(courseId);
        Lesson firstLesson = null;
        if (lessons != null && !lessons.isEmpty()) {
            firstLesson = lessons.get(0);
        }

        CourseAndFirstLessonResponse response = courseMapper.toCourseAndFirstLessonResponse(course);

        if (firstLesson != null) {
            response = lessonMapper.updateCourseAndFirstLessonResponse(firstLesson, response);
        }

        return response;
    }

    @Transactional
    public AdminCourseCreationResponse createGeneralStepInCourseCreation(
            GeneralCourseCreationRequest request,
            UUID userUuid, String userRole
    ) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Course course = courseMapper.toCourse(request);

        List<Category> categories = categoryRepository
                .findAllByIdIn(request.getCategoryIds());

        course.setCategories(categories);

        course.setCurrentCreationStep(1);
        course.setIsAvailable(false);
        course.setIsCompletedCreation(false);
        course.setScore(0);
        course.setAverageRating(0.0);
        course.setReviewCount(0);
        course.setUserId(userUuid);

        Course savedCourse = courseRepository.save(course);

        CourseSummary courseSummary = CourseSummary.builder()
                //.courseId(savedCourse.getCourseId())
                .courseName(savedCourse.getCourseName())
                .summaryContent(null)
                .course(savedCourse)
                .build();

        courseSummaryRepository.save(courseSummary);

        // Update the course with the course summary immediately before embedding
        courseRepository.flush();
        courseSummaryRepository.flush();

        //insertCourseVectorEmbedding(savedCourse.getCourseId());
        aiServiceClient.insertCourseEmbeddingData(savedCourse.getCourseId())
                .doOnSuccess(response -> log.info("Embedding data inserted for course {}", savedCourse.getCourseId()))
                .doOnError(error -> log.error("Error inserting embedding data for course {}: {}", savedCourse.getCourseId(), error.getMessage()))
                .subscribe();

        return courseMapper.toAdminCourseCreationResponse(savedCourse);
    }

    @Transactional
    public AdminCourseCreationResponse createFinalStepInCourseCreation(
            FinalCourseCreationRequest request, UUID courseId,
            UUID userUuid, String userRole
    ) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        course = courseMapper.updateCourse(request, course);

        course.setCurrentCreationStep(3);
        course.setIsAvailable(false);

        Course savedCourse = courseRepository.save(course);

        CourseSummary courseSummary = null;

        if (request.getAiSummaryContent() != null) {
            courseSummary = courseSummaryRepository.findById(courseId)
                    .orElse(
                            CourseSummary.builder()
                                    //.courseId(courseId)
                                    .courseName(savedCourse.getCourseName())
                                    .summaryContent(request.getAiSummaryContent())
                                    .course(savedCourse)
                                    .build());

            courseSummary.setSummaryContent(request.getAiSummaryContent());
            courseSummaryRepository.save(courseSummary);
        }

        savedCourse.setCourseSummary(courseSummary);

        return courseMapper.toAdminCourseCreationResponse(savedCourse);
    }

    @Transactional
    public AdminCourseCreationResponse updateGeneralStepInCourseCreation(
            GeneralCourseCreationRequest request,
            UUID courseId, UUID userUuid, String userRole
    ) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        course = courseMapper.updateCourse(request, course);

        List<Category> categories = categoryRepository
                .findAllByIdIn(request.getCategoryIds());

        course.setCategories(categories);

        Course savedCourse = courseRepository.save(course);

       /*CourseSummary courseSummary = savedCourse.getCourseSummary();

       if (courseSummary != null) {
           courseSummary.setCourseName(savedCourse.getCourseName());
           courseSummaryRepository.save(courseSummary);
       }*/

        CourseSummary courseSummary = courseSummaryRepository.findById(courseId)
                .orElse(
                        CourseSummary.builder()
                                .courseName(savedCourse.getCourseName())
                                .summaryContent(null)
                                .course(savedCourse)
                                .build()
                );

        courseSummaryRepository.save(courseSummary);

        courseRepository.flush();
        courseSummaryRepository.flush();

        //updateExistedCourseVectorEmbedding(courseId);
        aiServiceClient.updateCourseEmbeddingData(courseId)
                .doOnSuccess(response -> log.info("Embedding data updated for course {}", courseId))
                .doOnError(error -> log.error("Error updating embedding data for course {}: {}", courseId, error.getMessage()))
                .subscribe();

        return courseMapper.toAdminCourseCreationResponse(savedCourse);
    }

    @Transactional
    public AdminCourseCreationResponse updateFinalStepInCourseCreation(
            FinalCourseCreationRequest request, UUID courseId,
            UUID userUuid, String userRole
    ) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (course.getCurrentCreationStep() < 3) {
            throw new AppException(ErrorCode.COMPLETE_PREVIOUS_STEP_FIRST);
        }

        course = courseMapper.updateCourse(request, course);

        Course savedCourse = courseRepository.save(course);

        //CourseSummary courseSummary = savedCourse.getCourseSummary();

        CourseSummary courseSummary = null;

        if (request.getAiSummaryContent() != null) {
            courseSummary = courseSummaryRepository.findById(courseId)
                    .orElse(
                            CourseSummary.builder()
                                    //.courseId(courseId)
                                    .courseName(savedCourse.getCourseName())
                                    .summaryContent(request.getAiSummaryContent())
                                    .course(savedCourse)
                                    .build()
                    );

            courseSummary.setSummaryContent(request.getAiSummaryContent());
            courseSummaryRepository.save(courseSummary);
        }

        savedCourse.setCourseSummary(courseSummary);

        return courseMapper.toAdminCourseCreationResponse(savedCourse);
    }

    @Transactional
    public AdminCourseCreationResponse updateCourseAvailableStatus(
            Boolean availableStatus, UUID courseId,
            UUID userUuid, String userRole
    ) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (course.getCurrentCreationStep() < 3) {
            throw new AppException(ErrorCode.COURSE_NOT_COMPLETED);
        }


        if (availableStatus) {
            // auto update completed creation status to true
            course.setCurrentCreationStep(4);
            course.setIsCompletedCreation(true);

        } else {
            boolean existedUserEnrolled = userCoursesRepository.existsByEnrollId_CourseId(courseId);
            if (existedUserEnrolled) {
                throw new AppException(ErrorCode.COURSE_ALREADY_ENROLLED);
            }
        }

        course.setIsAvailable(availableStatus);

        Course savedCourse = courseRepository.save(course);

        return courseMapper.toAdminCourseCreationResponse(savedCourse);
    }

    @Transactional
    public AdminCourseCreationResponse updateCourseCompletedCreationStatus(
            Boolean completedCreationStatus, UUID courseId,
            UUID userUuid, String userRole
    ) {
        if (!userRole.equals(PredefinedRole.admin)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (course.getCurrentCreationStep() < 3) {
            throw new AppException(ErrorCode.COURSE_NOT_COMPLETED);
        }

        if (!completedCreationStatus) {
            course.setIsAvailable(false);

            boolean existedUserEnrolled = userCoursesRepository.existsByEnrollId_CourseId(courseId);

            if (existedUserEnrolled) {
                throw new AppException(ErrorCode.COURSE_ALREADY_ENROLLED);
            }
        }

        course.setIsCompletedCreation(completedCreationStatus);

        Course savedCourse = courseRepository.save(course);

        return courseMapper.toAdminCourseCreationResponse(savedCourse);
    }

    @Transactional
    public String uploadImage(MultipartFile file, UUID imageId) {
        try {
            String newPhotoUrl = cloudinaryService.uploadImage(file, imageId.toString(), "LessonAvatar");

            if (newPhotoUrl == null) {
                throw new AppException(ErrorCode.CANNOT_UPLOAD_IMAGE);
            }

            return newPhotoUrl;
        } catch (AppException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_UPLOAD_IMAGE);
        }
    }

    @Transactional
    public String uploadCourseAvatarImage(MultipartFile file, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
                );
        try {
            String newPhotoUrl = cloudinaryService.uploadImage(file, course.getCourseId().toString(), "CourseAvatar");

            if (newPhotoUrl == null) {
                throw new AppException(ErrorCode.CANNOT_UPLOAD_IMAGE);
            }

            course.setCourseImage(newPhotoUrl);

            courseRepository.save(course);

            return newPhotoUrl;
            //firebaseAuthClient.updateUserProfilePicture(userId, newPhotoUrl);
        } catch (AppException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_UPLOAD_IMAGE);
        }
    }

    @Transactional
    public String uploadCourseAvatarLink(String fileLink, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
                );
        try {
            if (course.getCourseImage() != null) {
                cloudinaryService.deleteImage(course.getCourseImage());
            }

            course.setCourseImage(fileLink);

            courseRepository.save(course);

            return fileLink;
            //firebaseAuthClient.updateUserProfilePicture(userId, newPhotoUrl);
        } catch (AppException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.CANNOT_UPLOAD_IMAGE);
        }
    }

    @Transactional
    public Boolean deleteCourseAvatarImage(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
                );

        try {
            if (course.getCourseImage() != null) {
                System.out.println("img url: " + course.getCourseImage());

                boolean result = cloudinaryService.deleteImage(course.getCourseImage());

                course.setCourseImage(null);
                courseRepository.save(course);
                courseRepository.flush();
                return result;
            }
            log.error("Course avatar image not found");
            return false;
        } catch (AppException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.CAN_NOT_DELETE_IMAGE);
        }
    }

    public List<CourseSearchResponse> getTopPaidCoursesByNumberOfEnrolledStudents(UUID userUuid, Integer limit) {
        if (limit == null) {
            limit = 10;
        }

        List<Course> courses = null;

        if (userUuid == null) {
            courses = courseRepository.findTopPaidCoursesByIsAvailableTrueOrderByNumberOfEnrolledStudentsDesc(limit);
        } else {
            courses = courseRepository.findTopPaidCoursesByIsAvailableTrueOrderAndExcludeUserEnrolledCoursesByNumberOfEnrolledStudentsDesc(userUuid, limit);
        }

        //courseRepository.findTop10ByIsAvailableTrueAndPriceGreaterThanOrderByAverageRatingDesc(0);

        List<CourseSearchResponse> responses = courses.stream()
                .map(course -> {
                    CourseSearchResponse response = courseMapper.toCourseSearchResponse(course);
                    int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;
                    response.setLessonCount(lessonCount);

                    response.setSections(null);

                    // because query course exclude course enrolled by user
                    response.setCertificateId(null);
                    response.setCertificateUrl(null);
                    return response;
                })
                .collect(Collectors.toList());


        return responses;
    }

    public Page<CourseSearchResponse> getFreeCourses(UUID userUuid, Pageable pageable) {
        Page<Course> courses = null;

        if (userUuid == null) {
            courses = courseRepository.findByIsAvailableTrueAndPriceEqualsZero(pageable);
        } else {
            courses = courseRepository.findByIsAvailableTrueAndPriceEqualsZeroAndExcludeUserEnrolledCourses(userUuid, pageable);
        }

        return courses.map(course -> {
            CourseSearchResponse response = courseMapper.toCourseSearchResponse(course);
            int lessonCount = course.getLessons() != null ? course.getLessons().size() : 0;
            response.setLessonCount(lessonCount);
            response.setSections(null);

            // because query course exclude course enrolled by user
            response.setCertificateId(null);
            response.setCertificateUrl(null);
            return response;
        });
    }

    @Async
    public void updateExistedCourseVectorEmbedding(UUID courseId) {
        log.info("Updating embedding data for course: {}", courseId);
        try {
            aiServiceClient.updateCourseEmbeddingData(courseId)
                    .doOnError(error -> log.error("Error updating embedding data for course {}: {}", courseId, error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to update embedding data for course {}: {}", courseId, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void deleteCourseVectorEmbedding(UUID courseId) {
        log.info("Deleting embedding data for course: {}", courseId);
        try {
            aiServiceClient.deleteCourseEmbeddingData(courseId)
                    .doOnError(error -> log.error("Error deleting embedding data for course {}: {}", courseId, error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to delete embedding data for course {}: {}", courseId, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void insertCourseVectorEmbedding(UUID courseId) {
        log.info("Inserting embedding data for course: {}", courseId);
        try {
            aiServiceClient.insertCourseEmbeddingData(courseId)
                    .doOnError(error -> log.error("Error inserting embedding data for course {}: {}", courseId, error.getMessage()))
                    .subscribe();
                    //.block(); // Use block() to wait for completion in async context

        } catch (Exception e) {
            log.error("Failed to insert embedding data for course {}: {}", courseId, e.getMessage());
            e.printStackTrace();
        }
    }
}
