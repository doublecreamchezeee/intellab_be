package com.example.courseservice.service;

import com.example.courseservice.constant.PredefinedLearningStatus;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.userCourses.EnrolledCourseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CategoryMapper;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.model.Category;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.LearningLesson;
import com.example.courseservice.model.UserCourses;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import com.example.courseservice.repository.*;
import com.example.courseservice.utils.ParseUUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
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

    public Page<CourseCreationResponse> getAllCourses(Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(course -> {
            int lessonCount = lessonRepository.countByCourse_CourseId(course.getCourseId());
            CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
            response.setLessonCount(lessonCount);
            return response;
        });
    }

    private <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

    public Page<CourseCreationResponse> getAllByCategory(Integer section, Pageable pageable) {

        Page<Course> result = courseRepository.findAllBySections_Id(section, pageable);

        return result.map(
                course -> {
                    int lessonCount = lessonRepository.countByCourse_CourseId(course.getCourseId());
                    CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
                    response.setLessonCount(lessonCount);
                    return response;
                }
        );
    }

    public Page<CourseCreationResponse> getAllCoursesExceptEnrolledByUser(UUID userId, Pageable pageable) {

        Page<Course> courses;
        if (userId == null) {
            courses = courseRepository.findAll(pageable);
        } else {
            courses = courseRepository.findAllCoursesExceptEnrolledByUser(userId, pageable);
        }
        return courses.map(course -> {
            int lessonCount = lessonRepository.countByCourse_CourseId(course.getCourseId());
            CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
            response.setLessonCount(lessonCount);
            return response;
        });
    }

    public void deleteCourseById(UUID id) {
        courseRepository.deleteById(id);
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

    public CourseCreationResponse updateCourse(UUID courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        courseMapper.updateCourse(course, request);
        course.setUserId(ParseUUID.normalizeUID(request.getUserUid()));

        /*List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        course.setLessons(lessons);*/

        course = courseRepository.save(course);
        return courseMapper.toCourseCreationResponse(course);
    }

    public Page<CourseCreationResponse> searchCoursesWithFilter(String keyword,
                                                                Float rating,
                                                                List<String> levels,
                                                                Boolean price,
                                                                List<Integer> categories,
                                                                Pageable pageable) {

        List<Course> coursesByKey = courseRepository.findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword,keyword);

        if(levels != null && !levels.isEmpty() ) {
            List<Course> coursesByKeyAndLevel = new ArrayList<>();
            for (String level : levels) {
                coursesByKeyAndLevel.addAll(courseRepository
                        .findAllByCourseNameContainingIgnoreCaseAndLevel(keyword, level));
                coursesByKeyAndLevel.addAll(courseRepository
                        .findAllByDescriptionContainingIgnoreCaseAndLevel(keyword,level));
            }

            coursesByKey.retainAll(coursesByKeyAndLevel);
        }

        if(price != null) {
            if (price)
            {
                coursesByKey.removeIf(course -> course.getPrice() == 0);
            }
            else
            {
                coursesByKey.removeIf(course -> course.getPrice() > 0);
            }

        }

        if (rating != null)
        {
            coursesByKey.removeIf(course -> course.getAverageRating() == null ||
                    course.getAverageRating().floatValue() < rating);
        }

        if (categories != null && !categories.isEmpty()) {
            List<Course> coursesByKeyAndCategory = new ArrayList<>();
            for (Integer category : categories) {
                coursesByKeyAndCategory.addAll(courseRepository.findAllByCourseNameContainingIgnoreCaseAndCategories_Id(keyword, category));
                coursesByKeyAndCategory.addAll(courseRepository.findAllByDescriptionContainingIgnoreCaseAndCategories_Id(keyword,category));
            }
            coursesByKey.retainAll(coursesByKeyAndCategory);
        }
        Page<Course> result = convertListToPage(coursesByKey, pageable);


        return result.map(course -> {
            int lessonCount = lessonRepository.countByCourse_CourseId(course.getCourseId());
            CourseCreationResponse response = courseMapper.toCourseCreationResponse(course);
            response.setLessonCount(lessonCount);
            return response;
        });
    }

    public Page<CourseCreationResponse> searchCourses(String keyword, Pageable pageable) {

        Page<Course>  courses = courseRepository.findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
        return courses.map(courseMapper::toCourseCreationResponse);
    }

    public DetailCourseResponse getCourseById(UUID courseId, UUID userUid) {
        // Fetch the course by ID or throw an exception if it doesn't exist
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Count the number of lessons in the course
        int lessonCount = lessonRepository.countByCourse_CourseId(courseId);

        // Calculate the average rating for the course
        double averageRating = course.getReviews().stream()
                .mapToDouble(review -> Optional.of(review.getRating()).orElse(0))
                .average()
                .orElse(0.0);

        // Count the number of reviews for the course
        int reviewCount = course.getReviews().size();

        // Check if the user is enrolled in the course
        boolean isUserEnrolled = userCoursesRepository.existsByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId);

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
            int totalLessons = lessonRepository.countByCourse_CourseId(courseId);
            int completedLessons = learningLessonRepository.countCompletedLessonsByUserIdAndLesson_Course_CourseIdAndIsDoneTheory(userUid, courseId, true);

            if (totalLessons > 0) {
                completionRatio = (completedLessons / (float) totalLessons) * 100;
            }
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
                .averageRating((float) averageRating)
                .reviewCount(reviewCount)
                .isUserEnrolled(isUserEnrolled)
                .latestLessonId(latestLessonId)
                .progressPercent(completionRatio)
                .build();
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


    public UserCourses enrollCourse(UUID userUid, UUID courseId) {
        if (userUid == null || courseId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Lấy đối tượng Course từ cơ sở dữ liệu
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        // Kiểm tra nếu đã có đăng ký khóa học này
        return userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
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
                            .build();

                    // create default learning lesson progress for user
                    lessonRepository.findAllByCourse_CourseIdOrderByLessonOrderDesc(courseId).forEach(lesson -> {
                        LearningLesson learningLesson = LearningLesson.builder()
                                .lesson(lesson)
                                .userId(userUid)
                                .status("NEW")
                                .assignments(new ArrayList<>())
                                .isDoneTheory(null)
                                .isDonePractice(false)
                                .build();
                        learningLessonRepository.save(learningLesson);
                    });



                    // Lưu đối tượng UserCourses mới vào cơ sở dữ liệu
                    return userCoursesRepository.save(newUserCourses);
                });
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

    public Page<UserCourses> getEnrolledCoursesOfUser(UUID userUid, Pageable pageable) {
        if (userUid == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        return userCoursesRepository.findAllByEnrollId_UserUid(userUid, pageable);
        /*return userCourses.map(userCourse -> {
            DetailCourseResponse detailCourseResponse = detailsCourseRepositoryCustom
                    .getDetailsCourse(userCourse.getEnrollId().getCourseId(), userUid)
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
            userCourse.setProgressPercent(detailCourseResponse.getProgressPercent());
            return userCourse;
        });*/
    }

    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::categoryToCategoryResponse).collect(Collectors.toList());
    }

}
