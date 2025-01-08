package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = OptionMapper.class)
public interface QuestionMapper {

    @Mapping(target = "order",ignore = true)
    @Mapping(target = "answer", ignore = true)
    @Mapping(target = "unitScore", ignore = true)
    @Mapping(source = "question_id", target = "questionId")
    @Mapping(source = "questionContent", target = "questionContent")
    @Mapping(source = "correct_answer", target = "correctAnswer")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "question_type", target = "questionType")
    @Mapping(source = "options", target = "options")
    QuestionResponse toQuestionResponse(Question question);


    @Mapping(target = "question_id", ignore = true)
    @Mapping(target = "questionContent", source = "questionContent")
    //@Mapping(target = "correct_answer", source = "correctAnswer")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "question_type", source = "questionType")
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    Question toQuestion(QuestionUpdateRequest questionUpdateRequest);

    @Mapping(target = "question_id", ignore = true)
    @Mapping(target = "questionContent", source = "questionContent")
    //@Mapping(target = "correct_answer", source = "correctAnswer")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "question_type", source = "questionType")
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    Question toQuestion(QuestionCreationRequest questionCreationRequest);

    @Mapping(target = "question_id", ignore = true)
    @Mapping(target = "questionContent", source = "questionContent")
    //@Mapping(target = "correct_answer", source = "correctAnswer")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "question_type", source = "questionType")
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    void updateQuestion(@MappingTarget Question question, QuestionUpdateRequest questionUpdateRequest);



}

