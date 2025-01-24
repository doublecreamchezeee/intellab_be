package com.example.courseservice.service;


import com.example.courseservice.dto.request.Assignment.AssignmentCreationRequest;
import com.example.courseservice.dto.request.Assignment.AssignmentDetailRequest;
import com.example.courseservice.dto.request.Assignment.SubmitAssignmentRequest;
import com.example.courseservice.dto.response.Assignment.AssignmentDetailResponse;
import com.example.courseservice.dto.response.Assignment.AssignmentResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.AssignmentDetailMapper;
import com.example.courseservice.mapper.AssignmentMapper;
import com.example.courseservice.model.*;
import com.example.courseservice.repository.*;
import com.example.courseservice.model.compositeKey.assignmentDetailID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentService {
    AssignmentRepository assignmentRepository;
    AssignmentMapper assignmentMapper;
    AssignmentDetailRepository assignmentDetailRepository;
    AssignmentDetailMapper assignmentDetailMapper;
    ExerciseRepository exerciseRepository;
    QuestionRepository questionRepository;
    LearningLessonRepository learningLessonRepository;

//    public AssignmentResponse getAssignmentById(UUID assignmentId) {
//        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));
//        return assignmentMapper.toResponse(assignment);
//    }
//
//    public AssignmentResponse addAssignment(AssignmentCreationRequest request) {
//        Assignment assignment = assignmentMapper.toAssignment(request);
//        Exercise exercise = exerciseRepository.findById(request.getExerciseId()
//        ).orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
//        assignment.setExercise(exercise);
//        LearningLesson learning = learningLessonRepository.findById(request.getLearningId()).orElseThrow(()->new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));
//        assignment.setLearningLesson(learning);
//
//        assignmentRepository.save(assignment);
//        return assignmentMapper.toResponse(assignment);
//    }
    @Transactional
    public Float submitAssignment(UUID lessonID, UUID userId, SubmitAssignmentRequest request) {
        LearningLesson learningLesson = learningLessonRepository.findById(request.getLearningId())
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_LESSON_NOT_FOUND));

        Assignment assignment = assignmentMapper.toAssignment(request);

        assignment.setLearningLesson(learningLesson);

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
        assignment.setExercise(exercise);


        Assignment result = assignmentRepository.saveAndFlush(assignment);

        List<AssignmentDetailRequest> detailRequests = request.getAssignmentDetailRequests();

        addDetail(result.getAssignment_id(), detailRequests);

        if(request.getScore() != null) {
            if(request.getScore() >= 8) {
                learningLesson.setIsDoneTheory(true);
            } else if (request.getScore() < 8) {
                learningLesson.setIsDoneTheory(false);
            }
            learningLessonRepository.save(learningLesson);
        }

        return assignment.getScore();
    }

    @Transactional
    public AssignmentResponse addDetail(UUID assignmentId,List<AssignmentDetailRequest> detailRequests) {
        if (assignmentId == null) {
            System.out.print("Assignment id is null");
            throw new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        detailRequests.forEach(detailRequest -> {
            //parse float fail unit score
            AssignmentDetail assignmentDetail = assignmentDetailMapper.toAssignmentDetail(detailRequest);
            assignmentDetailID id = new assignmentDetailID();
            id.setAssignment_id(assignmentId);
            id.setSubmit_order(detailRequests.indexOf(detailRequest)+1);
            //id.setSubmit_order(null);
            assignmentDetail.setAssignmentDetail_id(id);

            Question question = questionRepository.findById(detailRequest.getQuestionId()).orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
            assignmentDetail.setQuestion(question);
            assignmentDetail.setAssignment(assignment);
            assignmentDetailRepository.saveAndFlush(assignmentDetail);

        });

        assignmentRepository.save(assignment);
        return assignmentMapper.toAssignmentResponse(assignment);
    }


    public List<AssignmentDetailResponse> getAssignmentDetail(UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        List<AssignmentDetail> assignmentDetails = assignment.getAssignment_details();
        return assignmentDetails.stream().map(assignmentDetailMapper::toResponse).collect(Collectors.toList());
    }
}
