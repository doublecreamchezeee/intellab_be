package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.Assignment.SubmitAssignmentRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.UpdateOrderRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.service.AssignmentService;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/lessons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Lesson")
public class LessonController {
    LessonService lessonService;
    AssignmentService assignmentService;

    @Operation(
            summary = "Create lesson",
            hidden = true
    )
    @PostMapping
    ApiResponse<LessonResponse> createLesson(@RequestBody LessonCreationRequest request) {
        if (request.getClonedLessonId() == null)
        {
            return ApiResponse.<LessonResponse>builder()
                    .result(lessonService.createBlankLesson(request.getCourseId()))
                    .build();
        }
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.copyLesson(request.getClonedLessonId(),request.getCourseId()))
                .build();
    }

    @Operation(
            summary = "Get one lesson by id, provide userUid to check user has enrolled course or not, else 403"
    )
    @GetMapping("/{lessonId}")
    ApiResponse<DetailsLessonResponse> getLessonById(
            @PathVariable("lessonId") String lessonId,
            @RequestHeader ("X-UserId") String userUid
            //@PathVariable("userId") String userId
    ){
        userUid = userUid.split(",")[0];
        return ApiResponse.<DetailsLessonResponse>builder()
                .result(lessonService.getLessonById(
                        UUID.fromString(lessonId),
                        ParseUUID.normalizeUID(userUid))
                )
                .build();
    }

    @Operation(
            summary = "Delete lesson by id",
            hidden = true
    )
    @DeleteMapping("/{lessonId}")
    ApiResponse<String> deleteLesson(@PathVariable("lessonId") UUID lessonId){
        lessonService.removeLesson(lessonId);
        return ApiResponse.<String>builder()
                .result("Lesson has been deleted")
                .build();
    }

    @Operation(
            summary = "Update lesson by id",
            hidden = true
    )
    @PutMapping
    ApiResponse<LessonResponse> updateLesson(@RequestBody LessonUpdateRequest request){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.updateLesson(request))
                .build();
    }

    @Operation(
        summary = "update lessonOrder",
        hidden = true
    )
    @PutMapping("/lessonsOrder")
    ApiResponse<Boolean> updateLessonsOrder(@RequestBody UpdateOrderRequest request)
    {
        return ApiResponse.<Boolean>builder()
                .result(lessonService.updateLessonsOrder(request.getCourseId(),request.getLessonIds()))
                .build();
    }

    @Operation(
            summary = "Start learning lesson (fe don't need to use, be auto created when user enroll course)",
            hidden = true
    )
    @PostMapping("/startLesson")
    ApiResponse<LearningLessonResponse> startLesson(@RequestBody LearningLessonCreationRequest request){
        return ApiResponse.<LearningLessonResponse>builder()
                .result(
                        lessonService.createLearningLesson(
                        ParseUUID.normalizeUID(request.getUserId()),
                        request)
                )
                .build();
    }

    @Operation(
            summary = "Update learning progress of lesson",
            description = "Update learning progress of lesson by id, status can be 'new', 'inprogress', 'completed' (call before fetch detail lesson)"
    )
    @PutMapping("/{learningLessonId}/{courseId}/updateLearningProgress")
    ApiResponse<LearningLessonResponse> updateLearningProgress(
            @PathVariable("learningLessonId") UUID learningLessonId,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader("X-UserId") String userUid,
            //@PathVariable("userUid") String userUid,
            @RequestBody LearningLessonUpdateRequest request) {
        userUid = userUid.split(",")[0];
        return ApiResponse.<LearningLessonResponse>builder()
                .result(lessonService.updateLearningLesson(learningLessonId, courseId, userUid, request))
                .build();
    }



    @PostMapping("{lessonId}/submitquiz")
    ApiResponse<Float> submitQuiz(@PathVariable("lessonId") UUID lessonId,
                                  @RequestBody SubmitAssignmentRequest request,
                                  @RequestHeader("X-UserId") String UserUid)
    {
        UserUid = UserUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(UserUid);
        return ApiResponse.<Float>builder()
                .result(assignmentService.submitAssignment(lessonId,userId,request)).build();
    }


    @Operation(
            summary = "Get quiz of lesson"
    )
    @GetMapping("/{lessonId}/quiz")
    ApiResponse<List<QuestionResponse>> quiz(@PathVariable("lessonId") UUID lessonId,
                                             @RequestParam (required = false, defaultValue = "10") Integer numberOfQuestions,
                                             @RequestParam Boolean isGetAssignment,
                                             @RequestHeader ("X-UserId") String UserUid){

        if (isGetAssignment) {
            UserUid = UserUid.split(",")[0];
            UUID userid = ParseUUID.normalizeUID(UserUid);

            return ApiResponse.<List<QuestionResponse>>builder()
                    .result(lessonService.getLastAssignment(lessonId,userid))
                    .build();
        }
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(lessonService.getQuestion(lessonId, numberOfQuestions))
                .build();
    }



    @Operation(
            summary = "Mark theory of lesson as done"
    )
    @PutMapping("/{learningLessonId}/{courseId}/doneTheory")
    ApiResponse<Boolean> doneTheoryOfLesson(
            @PathVariable("learningLessonId") UUID learningLessonId,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader("X-UserId") String userUid
            //@PathVariable("userUid") String userUid
    ) throws Exception {
        userUid = userUid.split(",")[0];

        return ApiResponse.<Boolean>builder()
                .result(lessonService.doneTheoryOfLesson(
                            learningLessonId,
                            courseId,
                            ParseUUID.normalizeUID(userUid)
                        )
                )
                .build();
    }

    @Operation(
            summary = "Mark practice of lesson as done"
    )
    @PutMapping("/{learningLessonId}/{courseId}/donePractice")
    ApiResponse<Boolean> donePracticeOfLesson(
            @PathVariable("learningLessonId") UUID learningLessonId,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader("X-UserId") String userUid
            //@PathVariable("userUid") String userUid
    ) throws Exception {
        userUid = userUid.split(",")[0];

        return ApiResponse.<Boolean>builder()
                .result(lessonService.donePracticeOfLesson(
                            learningLessonId,
                            courseId,
                            ParseUUID.normalizeUID(userUid)
                        )
                )
                .build();
    }

    @Operation(
            summary = "(BE only) Mark practice of lesson as done by problem id"
    )
    @PutMapping("/{problemId}/{UserId}/donePracticeByProblemId")
    ApiResponse<Boolean> donePracticeOfLessonByProblemId(
            @PathVariable("problemId") UUID problemId,
           // @RequestHeader("X-UserId") String userUid
            @PathVariable("UserId") String userUid
    ) throws Exception {

        //userUid = userUid.split(",")[0];
        log.info("UserUid: " + UUID.fromString(userUid));
        return ApiResponse.<Boolean>builder()
                .result(lessonService.donePracticeOfLessonByProblemId(
                            problemId,
                            UUID.fromString(userUid)
                        )
                )
                .build();
    }

    @PostMapping("/upload")
    ApiResponse<String> uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam UUID courseId,
            @RequestHeader("X-UserId") String userUid
    )
    {
        UUID userId = ParseUUID.normalizeUID(userUid.split(",")[0]);
        try{
            return ApiResponse.<String>builder()
                    .code(201)
                    .message("File uploaded successfully")
                    .result(lessonService.uploadFile(file,courseId,userId))
                    .build();
        } catch (IOException e) {
            log.error("lessons/upload" + e.getMessage());
            return ApiResponse.<String>builder()
                    .code(401)
                    .message("File upload failed")
                    .build();
        }
    }

}
