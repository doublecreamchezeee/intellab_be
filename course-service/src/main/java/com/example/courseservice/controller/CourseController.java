package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.comment.CommentCreationRequest;
import com.example.courseservice.dto.request.comment.CommentModifyRequest;
import com.example.courseservice.dto.request.course.*;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.CourseSearchResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.dto.response.rerview.CourseReviewsStatisticsResponse;
import com.example.courseservice.dto.response.rerview.DetailsReviewResponse;
import com.example.courseservice.dto.response.userCourses.CertificateCreationResponse;
import com.example.courseservice.dto.response.userCourses.CompleteCourseResponse;
import com.example.courseservice.dto.response.userCourses.EnrolledCourseResponse;
import com.example.courseservice.dto.response.userCourses.UserCoursesResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.UserCourses;
import com.example.courseservice.service.CommentService;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.service.ReviewService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Course")
public class CourseController {
    CourseService courseService;
    LessonService lessonService;
    ReviewService reviewService;
    private final CommentService commentService;
    final String defaultRole = "myRole";
    private Boolean isAdmin(String role)
    {
        return role.contains("admin");
    }

    @Operation(
            summary = "Create course",
            description = "- Create a new course.\n" +
                    "- Only admin."
    )
    @PostMapping("")
    ApiResponse<CourseCreationResponse> createCourse(
            @RequestBody @Valid CourseCreationRequest request,
            @RequestHeader("X-UserRole") String role) {
        if (!isAdmin(role))
        {
            return ApiResponse.<CourseCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<CourseCreationResponse>builder()
                .code(201)
                .message("Created")
                .result(courseService.createCourse(
                        ParseUUID.normalizeUID(request.getUserUid()),
                        request))
                .build();
    }

    @Operation(
            summary = "Get all lessons of a course (using when user hasn't enrolled in course)"
    )
    @GetMapping("/{courseId}/lessons")
    ApiResponse<Page<LessonResponse>> getLessonsByCourseId(
            @PathVariable("courseId") String courseId,
            @ParameterObject Pageable pageable) {
        return ApiResponse.<Page<LessonResponse>>builder()
                .result(lessonService.getLessonsByCourseId(
                            courseId,
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get all lessons and progress of learning lessons in a course (using when user has enrolled in course)"
    )
    @GetMapping("/{courseId}/lessons/me") ///{userUid}
    ApiResponse<Page<LessonProgressResponse>> getLessonProgressByCourseIdAndUserUid(
            @PathVariable("courseId") String courseId,
            //@PathVariable("userUid") String userUid,
            @RequestHeader("X-UserId") String userUid,
            @ParameterObject Pageable pageable) {
        userUid = userUid.split(",")[0];
        log.info("UserUid: " + ParseUUID.normalizeUID(userUid));
        return ApiResponse.<Page<LessonProgressResponse>>builder()
                .result(lessonService.getLessonProgress(
                            ParseUUID.normalizeUID(userUid),
                            UUID.fromString(courseId),
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get a course by id, if userUid is provided, return isUserEnrolled is true if user has enrolled in course"
    )
    @GetMapping("/{courseId}")
    ApiResponse<DetailCourseResponse> getCourseById(@PathVariable("courseId") UUID courseId,
                                                    @RequestHeader(required = false, name = "X-UserId") String userUid
    ){
        log.info("UserUid: " + userUid);

        UUID userUUID = null;
        if (userUid != null) {

            userUid = userUid.split(",")[0];
            userUUID = ParseUUID.normalizeUID(userUid);
        }
        return ApiResponse.<DetailCourseResponse>builder()
                .result(courseService.getCourseById(courseId, userUUID))
                .build();
    }

    @Operation(
            summary = "Get all courses"
    )
    @GetMapping("")
    ApiResponse<Page<CourseCreationResponse>> getAllCourse(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) Integer Section) {
        if (Section != null)
        {
            return ApiResponse.<Page<CourseCreationResponse>>builder()
                    .result(courseService.getAllByCategory(
                            Section,
                            pageable
                            )
                    )
                    .build();
        }

        return ApiResponse.<Page<CourseCreationResponse>>builder()
                .result(courseService.getAllCourses(
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get details of multiple courses by their IDs"
    )
    @PostMapping("/details")
    ApiResponse<List<DetailCourseResponse>> getDetailsOfMultipleCourses(
            @RequestBody Map<String, List<String>> requestBody,
            @RequestHeader(required = false, name = "X-UserId") String userUid) {

        UUID userUUID = null;
        if (userUid != null) {
            userUid = userUid.split(",")[0];
            userUUID = ParseUUID.normalizeUID(userUid);
        }

        // Retrieve the list of course IDs from the map
        List<String> courseIdsList = requestBody.get("courseIds");

        // Convert the list of course IDs (String) to UUID
        List<UUID> courseUUIDs = courseIdsList.stream()
                .map(UUID::fromString)  // Convert each course ID string to UUID
                .collect(Collectors.toList());

        // Call the service to get the course details
        return ApiResponse.<List<DetailCourseResponse>>builder()
                .result(courseService.getDetailsOfCourses(courseUUIDs, userUUID))
                .build();
    }

    @Operation(
            summary = "Get all courses except enrolled courses by user"
    )
    @GetMapping("/exceptEnrolled")
    ApiResponse<Page<CourseCreationResponse>> getAllCourseExceptEnrolledByUser(
            @RequestHeader(required = false, name = "X-UserId") String userUid,
            @ParameterObject Pageable pageable) {

        UUID userUUID = null;

        if (userUid != null) {
            userUid = userUid.split(",")[0];
            userUUID = ParseUUID.normalizeUID(userUid);
        }

        return ApiResponse.<Page<CourseCreationResponse>>builder()
                .result(courseService.getAllCoursesExceptEnrolledByUser(
                        userUUID,
                        pageable
                ))
                .build();
    }

    @Operation(
            summary = "Delete a course by id"
    )
    @DeleteMapping("/{courseId}")
    ApiResponse<String> deleteCourseById(
            @PathVariable("courseId") UUID courseId,
            @RequestHeader("X-UserID") String userUid,
            @RequestHeader("X-UserRole") String role) {
        if (!isAdmin(role)) {
            return ApiResponse.<String>builder()
                    .code(201)
                    .result("You are not allowed to delete this course")
                    .message("Forbidden").build();
        }
        userUid = userUid.split(",")[0];
        courseService.deleteCourseById(courseId, userUid);
        return ApiResponse.<String>builder()
                .code(204)
                .message("Delete course by id: " + courseId)
                .result("Course has been deleted")
                .build();
    }

    @Operation(
            summary = "Update a course by id"
    )
    @PutMapping("/{courseId}")
    ApiResponse<CourseCreationResponse> updateCourse(
            @PathVariable("courseId") UUID courseId,
            @RequestBody CourseUpdateRequest request,
            @RequestHeader("X-UserId") String userUid,
            @RequestHeader("X-UserRole") String role) {
        if (!isAdmin(role)) {
            return ApiResponse.<CourseCreationResponse>builder()
                    .code(201)
                    .result(null)
                    .message("Forbidden").build();
        }
        return ApiResponse.<CourseCreationResponse>builder()
                .result(courseService.updateCourse(courseId, request, userUid.split(",")[0 ]))
                .build();
    }

    @Operation(
            summary = "Get all courses that contain keyword in title or description"
    )
    @GetMapping("/search")
    public ApiResponse<Page<CourseSearchResponse>> searchCourses(
            @RequestHeader(value = "X-UserID", required = false) String userUid,
            @RequestParam("keyword") String keyword,
            @RequestParam(required = false) Float ratings,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Boolean price,
            @RequestParam(required = false) List<Integer> categories,
            @ParameterObject Pageable pageable) {

        UUID normalizedUserId = null;
        if (userUid != null) {
            userUid = userUid.split(",")[0];
            normalizedUserId = ParseUUID.normalizeUID(userUid);
        }

        System.out.println(userUid);
        if (ratings != null || levels != null || price != null || categories != null) {
            return ApiResponse.<Page<CourseSearchResponse>>builder()
                    .result(courseService.searchCoursesWithFilter(
                            normalizedUserId,
                            keyword,ratings,levels,price,categories, pageable
                            )
                    ).build();
        }

        return ApiResponse.<Page<CourseSearchResponse>>builder()
                .result(courseService.searchCourses(
                            normalizedUserId,
                            keyword, pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Enroll a course (both free and paid - BE auto check condition)",
            description = """
                This API is used to enroll a course.
                If the course is free, the user will be enrolled immediately.
                If the course is paid, the user will be enrolled if the user has a subscription plan.
                """
    )
    @PostMapping("/enroll")
    public ApiResponse<UserCourses> enrollCourse(
            @RequestBody @Valid EnrollCourseRequest request,
            @RequestHeader("X-UserRole") String role
    ) {
        if (isAdmin(role)) {
            return ApiResponse.<UserCourses>builder()
                    .code(201)
                    .message("Forbidden")
                    .result(null)
                    .build();
        }

        if (role == null || role.equals(defaultRole)) {
            role = "user,free";
        }

        //log.info("role: {}", (Object) role.split(","));
        String subscriptionPlan = role.split(",")[1];

        return ApiResponse.<UserCourses>builder()
                .result(
                        courseService.enrollCourse(
                                ParseUUID.normalizeUID(request.getUserUid()),
                                request.getCourseId(),
                                subscriptionPlan
                        )
                )
                .build();
    }

    @Operation(
            summary = "Enroll a paid course",
            description = "This API is used to enroll a paid course.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The request body contains the user UID and the course ID.",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = EnrollCourseRequest.class)
                    )
            ),
            hidden = true
    )
    @PostMapping("/enrollPaidCourse")
    public ApiResponse<UserCourses> enrollPaidCourse(@RequestBody @Valid EnrollCourseRequest request) {

        return ApiResponse.<UserCourses>builder()
                .message("Course has been enrolled successfully")
                .result(courseService.enrollPaidCourse(
                            ParseUUID.normalizeUID(request.getUserUid()),
                            request.getCourseId(),
                            false // user enroll by purchase exactly this course
                        )
                )
                .build();
    }

    @Operation(
            summary = "(BE only) Disenroll a course by user id and course id",
            hidden = true
    )
    @PostMapping("/disenroll")
    public ApiResponse<Boolean> disenrollCourse(@RequestBody @Valid DisenrollCourseRequest request) {
        Boolean result = courseService.disenrollCourse(
                ParseUUID.normalizeUID(
                        request.getUserUid()
                ),
                request.getCourseId()
        );
        return ApiResponse.<Boolean>builder()
                .message("Course has been disenrolled")
                .result(result)
                .build();
    }

    @Operation(
            summary = "Get all users have enrolled a course"
    )
    @GetMapping("/{courseId}/enrolledUsers")
    public ApiResponse<List<EnrolledCourseResponse>> getEnrolledUsersOfCourse(@PathVariable("courseId") UUID courseId) {
        return ApiResponse.<List<EnrolledCourseResponse>>builder()
                .result(courseService.getEnrolledUsersOfCourse(courseId))
                .build();
    }

    @GetMapping("/courseList/me")
    public ApiResponse<List<CompleteCourseResponse>> getCourseByUserId(
            @RequestHeader(name = "X-UserId", required = false) String userUid,
            @RequestHeader(name = "X-UserRole", required = false) String role,
            @RequestParam (required = false) String UserUid) {
//        if (isAdmin(role)) {
//            return ApiResponse.<List<CompleteCourseResponse>>builder()
//                    .code(201)
//                    .message("Forbidden")
//                    .result(null)
//                    .build();
//        }
        if (userUid == null && UserUid == null) {
            throw new AppException(ErrorCode.INVALID_USER);
        }

        if (userUid != null) {
            return ApiResponse.<List<CompleteCourseResponse>>builder()
                    .result(courseService.getCompleteCourseByUserId(ParseUUID.normalizeUID(userUid)))
                    .build();
        }

        userUid = userUid.split(",")[0];

        System.out.println(userUid);
        System.out.println(ParseUUID.normalizeUID(userUid));

        return ApiResponse.<List<CompleteCourseResponse>>builder()
                .result(courseService.getCompleteCourseByUserId(ParseUUID.normalizeUID(userUid)))
                .build();
    }

    @Operation(
            summary = "Get all courses that a user has enrolled"
    )
    @GetMapping("/me/enrolledCourses")
    public ApiResponse<Page<UserCourses>> getEnrolledCoursesOfUser(
            //@PathVariable("userUid") String userUid,
            @RequestHeader("X-UserId") String userUid,
            @RequestHeader("X-UserRole") String role,
            @ParameterObject Pageable pageable) {
        if(isAdmin(role)) {
            return ApiResponse.<Page<UserCourses>>builder()
                    .result(null)
                    .message("Forbidden: this is admin account")
                    .code(201)
                    .build();
        }
        userUid = userUid.split(",")[0];
        log.info("UserUid: " + ParseUUID.normalizeUID(userUid));
        return ApiResponse.<Page<UserCourses>>builder()
                .result(courseService.getEnrolledCoursesOfUser(
                            ParseUUID.normalizeUID(userUid),
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get all review of a course by course id"
    )
    @GetMapping("/{courseId}/reviews")
    public ApiResponse<Page<DetailsReviewResponse>> getReviewsByCourseId(
            @PathVariable("courseId") UUID courseId,
            @RequestParam(required = false) Integer rating,
            @ParameterObject Pageable pageable) {

        return ApiResponse.<Page<DetailsReviewResponse>>builder()
                .result(reviewService.getAllReviewsByCourseId(courseId, rating, pageable))
                .build();
    }

    @Operation(
            summary = "Get all categories"
    )
    @GetMapping("categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {

        return ApiResponse.<List<CategoryResponse>>builder()
                .result(courseService.getCategories()).build();
    }

    @Operation(summary = "Get category by Id")
    @GetMapping("/category/{categoryId}")
    public ApiResponse<CategoryResponse> getCategoryById(
            @PathVariable Integer categoryId )
    {
        return ApiResponse.<CategoryResponse>builder()
                .result(courseService.getCategory(categoryId)).build();
    }

    @Operation(summary = "Get category by list of id")
    @PostMapping("/category")
    public ApiResponse<List<CategoryResponse>> getCategoryByListOfId(@RequestBody List<Integer> listOfId) {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(courseService.getListCategory(listOfId)).build();
    }

    @Operation(
            summary = "Tự động tạo khi (progress - 100 <= 1e-6f)"
    )
    @PostMapping("{courseId}/certificate")
    public ApiResponse<CertificateCreationResponse> generateCertificate(
            @PathVariable UUID courseId,
            @RequestHeader (value = "X-UserId") String userUid
            ) throws Exception {
        System.out.println("userUid:" + userUid);

        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);


        CertificateCreationResponse result = courseService.createCertificate(courseId, userId);


        return ApiResponse.<CertificateCreationResponse>builder()
                .result(result).build();
    }

    @GetMapping("{certificateId}/certificate")
    public ApiResponse<Object> getCertificate(
            @PathVariable UUID certificateId
    ) throws ExecutionException, InterruptedException {

        return ApiResponse.builder()
                .result(courseService.getCertificate(certificateId)).build();
    }


    @Operation(
            summary = "Get review statistics of course by course id"
    )
    @GetMapping("/{courseId}/reviews-stats")
    public ApiResponse<CourseReviewsStatisticsResponse> getReviewStatisticsByCourseId(
            @PathVariable("courseId") UUID courseId) {
        return ApiResponse.<CourseReviewsStatisticsResponse>builder()
                .result(reviewService.getCourseReviewsStatisticsByCourseId(courseId))
                .build();
    }

    @Operation(
            summary = "(testing only) complete all lesson of course by course id"
    )
    @PostMapping("/{courseId}/completeAllLessons")
    public ApiResponse<String> completeAllLessonsOfCourse(@PathVariable("courseId") UUID courseId,
                                                          @RequestHeader("X-UserId") String userUid) {
        userUid = userUid.split(",")[0];
        lessonService.completeAllLessonByCourseId(
                courseId,
                ParseUUID.normalizeUID(userUid)
        );

        return ApiResponse.<String>builder()
                .result("All lessons of course have been completed")
                .build();
    }

    @Operation(
            summary = "(testing only) restart all lessons of course by course id"
    )
    @PostMapping("/{courseId}/restartAllLessons")
    public ApiResponse<String> restartAllLessonsOfCourse(@PathVariable("courseId") UUID courseId,
                                                          @RequestHeader("X-UserId") String userUid) {
        userUid = userUid.split(",")[0];
        lessonService.restartAllLessonByCourseId(
                courseId,
                ParseUUID.normalizeUID(userUid)
        );

        return ApiResponse.<String>builder()
                .result("All lessons of course have been restarted")
                .build();
    }

    @Operation(
            summary = "Lấy tất cả comment của course theo courseId",
            description =
            """
                    cách truyền params sort outer: sort=properties,order\s
                    order : gồm có asc, desc
                    
                    properties gồm có:\s
                    numberOfLikes: số upvote
                    lastModified: thời gian cập nhật
                    created: thời gian tạo
                    
                    ví dụ
                    sort=numberOfLikes,asc&sort=lastModified,desc
                    
                    cách truyền params sort inner:
                    childrenSortBy=property&childrenSortOrder=order\
                    
                    kích thước mặt định (size): default của outer = 20, default inter là 5"""
    )
    @GetMapping("/{courseId}/comments")
    public ApiResponse<Page<CommentResponse>> getCommentsByCourseId(
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(required = false, name = "X-UserId") String userUid,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "childrenPage", required = false, defaultValue = "0") Integer childrenPage,
            @RequestParam(name = "childrenSize", required = false, defaultValue = "5") Integer childrenSize,
            @RequestParam(defaultValue = "lastModified", required = false) String childrenSortBy,
            @RequestParam(defaultValue = "asc", required = false) String childrenSortOrder
    ) {
        UUID userId = null;

        if (userUid != null)
        {
            userUid = userUid.split(",")[0];
            userId = ParseUUID.normalizeUID(userUid);
        }

        if (childrenPage == null) {
            childrenPage = 0;
        }

        if (childrenSize == null) {
            childrenSize = 20;
        }

        Sort sort = childrenSortOrder.equalsIgnoreCase("desc")
                ? Sort.by(childrenSortBy).descending()
                : Sort.by(childrenSortBy).ascending();

        Pageable childrenPageable = PageRequest.of(childrenPage, childrenSize, sort);


        return ApiResponse.<Page<CommentResponse>>builder()
                .result(commentService.getComments(courseId, userId, pageable, childrenPageable))
                .build();
    }

    @Operation(
            summary = "Lấy comment và children comment theo commentId",
            description = """
                    Lấy comment theo commentId với số lượng children comment được truyền vào tùy ý (size = ?) default 20
                    cách truyền params sort: sort=properties,order\s
                    order : gồm có asc, desc
                    
                    properties gồm có:\s
                    numberOfLikes: số upvote
                    lastModified: thời gian cập nhật
                    created: thời gian tạo
                    
                    ví dụ
                    sort=numberOfLikes,asc&sort=lastModified,desc
                    """
    )
    @GetMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> getComment(
            @PathVariable("commentId") UUID commentId,
            @RequestHeader(required = false, name = "X-UserId") String userUid,
            @ParameterObject Pageable pageable
    )
    {
        UUID userId = null;

        if (userUid != null)
        {
            userUid = userUid.split(",")[0];
            userId = ParseUUID.normalizeUID(userUid);
        }
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.getComment(commentId, userId, pageable))
                .build();
    }


    @Operation(
            summary =  "Lấy page comment con theo commentId",
            description =
            """
                    Lấy page comment con theo commentId với số lượng được truyền vào tùy ý (size = ?) default 20
                    cách truyền params sort: sort=properties,order\s
                    order : gồm có asc, desc
                    
                    properties gồm có:\s
                    numberOfLikes: số upvote
                    lastModified: thời gian cập nhật
                    created: thời gian tạo
                    
                    ví dụ
                    sort=numberOfLikes,asc&sort=lastModified,desc
                    """
    )
    @GetMapping("/comments/{commentId}/children")
    public ApiResponse<Page<CommentResponse>> getChildrenComments(
            @PathVariable("commentId") UUID commentId,
            @RequestHeader(required = false, name = "X-UserId") String userUid,
            @ParameterObject Pageable pageable
    )
    {
        UUID userId = null;

        if (userUid != null)
        {
            userUid = userUid.split(",")[0];
            userId = ParseUUID.normalizeUID(userUid);
        }
        return ApiResponse.<Page<CommentResponse>>builder()
                .result(commentService.getChildrenComments(commentId, userId, pageable))
                .build();
    }


    @PostMapping("/{courseId}/comments")
    public ApiResponse<CommentResponse> addComment(
            @RequestHeader("X-UserId") String userUid,
            @PathVariable("courseId") UUID courseId,
            @RequestBody CommentCreationRequest creationRequest ){
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);

        return ApiResponse.<CommentResponse>builder()
                .result(commentService.addComment(courseId, creationRequest, userId)).build();
    }

    @PutMapping("/comments/{commentId}/upvote")
    public ApiResponse<CommentResponse> upVoteComment(
            @RequestHeader("X-UserId") String userUid,
            @PathVariable("commentId") UUID commentId){
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);

        return ApiResponse.<CommentResponse>builder()
                .result(commentService.upvoteComment(userId, commentId)).build();
    }


    @PutMapping("/comments/{commentId}/cancelUpvote")
    public ApiResponse<CommentResponse> cancelUpvoteComment(
            @RequestHeader("X-UserId") String userUid,
            @PathVariable("commentId") UUID commentId ) {
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);

        return ApiResponse.<CommentResponse>builder()
                .result(commentService.cancelUpvoteComment(userId, commentId)).build();
    }

    @PutMapping("/comments/modify")
    public ApiResponse<CommentResponse> modifyComment(
            @RequestHeader("X-UserId") String userUid,
            @RequestBody CommentModifyRequest modifyRequest ){
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.ModifyComment(userId, modifyRequest)).build();
    }

    @DeleteMapping("/comments/{commentId}/delete")
    public ApiResponse<Boolean> deleteComment(
            @RequestHeader("X-UserId") String userUid,
            @PathVariable("commentId") UUID commentId ){
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);
        return ApiResponse.<Boolean>builder()
                .result(commentService.removeComment(commentId, userId)).build();
    }

    @Operation(
            summary = "testing only"
    )
    @GetMapping("/role")
    public String getRole(
            @RequestHeader("X-UserRole") String role
    )
    {
        return role;
    }

    @Operation(
            summary = "Disenroll all courses that user has enrolled using subscription plan",
            hidden =   true
    )
    @PostMapping("disenroll-courses-enrolled-using-subscription-plan")
    public ApiResponse<Boolean> disenrollCoursesEnrolledUsingSubscriptionPlan(
            @RequestBody DisenrollCoursesEnrolledUsingSubscriptionPlanRequest request) {
        return ApiResponse.<Boolean>builder()
                .message("All courses have been disenrolled")
                .result(courseService.disenrollCoursesEnrolledUsingSubscriptionPlan(
                            request.getListUserUuid()
                        )
                )
                .build();
    }

    @Operation(
            summary = "Check if user already enrolled in course",
            hidden = true
    )
    @PostMapping("/check-enrolled")
    public ApiResponse<Boolean> checkUserCourseExisted(
            @RequestBody CheckingUserCourseExistedRequest request) {
        return ApiResponse.<Boolean>builder()
                .result(courseService.hasUserAlreadyEnrollCourse(
                        request
                    )
                )
                .build();
    }

}
