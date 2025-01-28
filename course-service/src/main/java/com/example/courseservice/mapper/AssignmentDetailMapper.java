package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Assignment.AssignmentDetailRequest;
import com.example.courseservice.dto.response.Assignment.AssignmentDetailResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.model.AssignmentDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper( componentModel = "spring", uses = QuestionMapper.class)
public interface AssignmentDetailMapper {
    @Mapping(target = "order",source = "assignmentDetailId.submitOrder")
    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "unitScore", source = "unitScore")
    @Mapping(target = "question",source = "question")
    AssignmentDetailResponse toResponse(AssignmentDetail assignmentDetail);


    @Mapping(target = "order",source = "assignmentDetailId.submitOrder")
    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "unitScore", source = "unitScore")
    @Mapping(source = "question.questionId", target = "questionId")
    @Mapping(source = "question.questionContent", target = "questionContent")
    @Mapping(source = "question.correctAnswer", target = "correctAnswer")
    @Mapping(source = "question.status", target = "status")
    @Mapping(source = "question.questionType", target = "questionType")
    @Mapping(source = "question.options", target = "options")
    QuestionResponse toQuestionResponse(AssignmentDetail assignmentDetail);

    @Mapping(target = "answer", source = "answer")
    @Mapping(target = "unitScore", source = "unitScore")
    AssignmentDetail toAssignmentDetail(AssignmentDetailRequest assignmentDetailRequest);
}


//-----response---------------
//Integer order;
//String answer;
//Integer unitScore;
//QuestionResponse question;
//----------------------------

//-----------entity-----------
//AssignmentDetailID assignmentDetail_id;
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
