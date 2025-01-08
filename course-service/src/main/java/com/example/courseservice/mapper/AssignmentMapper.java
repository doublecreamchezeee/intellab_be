package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Assignment.AssignmentCreationRequest;
import com.example.courseservice.dto.request.Assignment.SubmitAssignmentRequest;
import com.example.courseservice.dto.response.Assignment.AssignmentResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.model.Assignment;
import com.example.courseservice.model.AssignmentDetail;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.LearningLesson;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = AssignmentDetailMapper.class)
public interface AssignmentMapper {

    @Mapping(target = "assignment_id", ignore = true)
    @Mapping(target = "score", source = "score")
    @Mapping(target = "submit_order", ignore = true)
    @Mapping(target = "assignment_details", source = "assignmentDetailRequests")
    Assignment toAssignment(SubmitAssignmentRequest request);
}
//--------------------------------
//          Response
//UUID assignmentId;
//UUID learningLessonId;
//ExerciseResponse exercise;
//Float score;
//Integer submitOrder;
//Instant submitDate;
//---------------------------------

//-----------------------------------
//          Entity
//UUID assignment_id;
//Integer submit_order;
//Float score;
//Instant submit_date;
//Exercise exercise;
//LearningLesson learningLesson;
//List<AssignmentDetail> assignment_details;
//------------------------------------