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
import com.example.courseservice.repository.impl.DetailsCourseRepositoryCustomImpl;
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
    DetailsCourseRepositoryCustomImpl detailsCourseRepositoryCustom;
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

    public Page<CourseCreationResponse> getAllByCategory(String categoryName, Pageable pageable)
    {
        Boolean isFeature = true;
        List<Category> categories = categoryRepository.findAllByNameAndIsFeatured(categoryName,isFeature);

        if (categories.isEmpty())
        {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Page<Course> result = courseRepository.findAllByCategories_Name(categoryName, pageable);

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

        course.setUserUid(userUid);
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
        course.setUserUid(ParseUUID.normalizeUID(request.getUserUid()));

        /*List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        course.setLessons(lessons);*/

        course = courseRepository.save(course);
        return courseMapper.toCourseCreationResponse(course);
    }

    public Page<CourseCreationResponse> searchCoursesWithFilter(String keyword,
                                                                Float rating,
                                                                String level,
                                                                Boolean price,
                                                                List<String> categories,
                                                                Pageable pageable) {

        List<Course> coursesByKey = courseRepository.findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword,keyword);

        if(level != null) {
            List<Course> coursesByKeyAndLevel = courseRepository
                    .findAllByCourseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndLevel(keyword, keyword, level);
            coursesByKey.retainAll(coursesByKeyAndLevel);
        }

        if(price != null) {
            if (price)
            {
                Iterator<Course> courseIterator = coursesByKey.iterator();
                while (courseIterator.hasNext()) {
                    Course course = courseIterator.next();
                    if(course.getPrice() == 0){
                        courseIterator.remove();
                    }
                }
            }
            else
            {
                Iterator<Course> courseIterator = coursesByKey.iterator();
                while (courseIterator.hasNext()) {
                    Course course = courseIterator.next();
                    if(course.getPrice() > 0){
                        courseIterator.remove();
                    }
                }
            }

        }

        if (rating != null)
        {
            Iterator<Course> courseIterator = coursesByKey.iterator();
            while (courseIterator.hasNext()) {
                Course course = courseIterator.next();
                if(course.getAverageRating() < rating){
                    courseIterator.remove();
                }
            }
        }

        if (categories != null && !categories.isEmpty()) {
            for (String category : categories) {
                List<Course> coursesByNameAndCategory = courseRepository.findAllByCourseNameContainingIgnoreCaseAndCategories_Name(keyword, category);

                List<Course> coursesByDescriptionAndCategory = courseRepository.findAllByDescriptionContainingIgnoreCaseAndCategories_Name(keyword,category);


                // union 2 list lại
                List<Course> coursesByKeyAndCategory = new ArrayList<>(coursesByNameAndCategory);
                coursesByKeyAndCategory.addAll(coursesByDescriptionAndCategory);


                coursesByKey.retainAll(coursesByKeyAndCategory);
            }
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
        // Check if a userUid is provided
        /*if (userUid != null) {
            // Find UserCourses by composite key (userUid and courseId)
            UserCourses userCourses = userCoursesRepository.findByEnrollId_UserUidAndEnrollId_CourseId(userUid, courseId)
                    .orElse(null);

            // If user is not enrolled in the course, fetch course details without enrollment flag
            if (userCourses == null) {
                return courseMapper.toDetailCourseResponse(
                        courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED)),
                        false // User is not enrolled
                );
            }

            // If user is enrolled in the course, fetch course details
            return courseMapper.toDetailCourseResponse(
                    courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED)),
                    true // Mark that the user is enrolled
            );
        }

        // If userUid is null, fetch course details without enrollment flag
        return courseMapper.toDetailCourseResponse(
                courseRepository.findById(courseId).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED)),
                false // User is not enrolled
        );*/

        DetailCourseResponse detailCourseResponse = detailsCourseRepositoryCustom
                .getDetailsCourse(courseId, userUid)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        return detailCourseResponse;


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
                                .isDoneTheory(false)
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

        Page<UserCourses> userCourses = userCoursesRepository.findAllByEnrollId_UserUid(userUid, pageable);

        return userCourses;
        /*return userCourses.map(userCourse -> {
            DetailCourseResponse detailCourseResponse = detailsCourseRepositoryCustom
                    .getDetailsCourse(userCourse.getEnrollId().getCourseId(), userUid)
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));
            userCourse.setProgressPercent(detailCourseResponse.getProgressPercent());
            return userCourse;
        });*/
    }

    public List<CategoryResponse> getCategories(String Type) {
        List<Category> categories = categoryRepository.findAllByType(Type);

        return categories.stream().map(categoryMapper::categoryToCategoryResponse).collect(Collectors.toList());
    }

}
