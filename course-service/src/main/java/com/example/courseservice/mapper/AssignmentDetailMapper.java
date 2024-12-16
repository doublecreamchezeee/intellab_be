package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Assignment.AssignmentDetailRequest;
import com.example.courseservice.dto.response.Assignment.AssignmentDetailResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.model.Assignment;
import com.example.courseservice.model.AssignmentDetail;
import com.example.courseservice.model.Question;
import com.example.courseservice.model.compositeKey.assignmentDetailID;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper( componentModel = "spring", uses = QuestionMapper.class)
public interface AssignmentDetailMapper {
    @Mapping(target = "order",source = "assignmentDetail_id.submit_order")
    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "unitScore", source = "unit_score")
    @Mapping(target = "question",source = "question")
    AssignmentDetailResponse toResponse(AssignmentDetail assignmentDetail);


    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "unit_score", source = "unitScore")
    AssignmentDetail toAssignmentDetail(AssignmentDetailRequest assignmentDetailRequest);
}


//-----response---------------
//Integer order;
//String answer;
//Integer unitScore;
//QuestionResponse question;
//----------------------------

//-----------entity-----------
//assignmentDetailID assignmentDetail_id;
//{     UUID assignment_id;
//      Integer submit_order;
//}
//Float unit_score;
//// câu trả lời có thể là single choice hoặc multi-choice
//// có thêm ràng buộc cho câu trả lời
//String answer;
//Question question;
//Assignment assignment;
//----------------------------


//----------------------------
//    request
//
//String answer;
//String unitScore;
//
//UUID questionId;
//----------------------------
