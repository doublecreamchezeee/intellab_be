package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionCreationResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = OptionMapper.class)
public interface QuestionMapper {

    @Mapping(target = "order",ignore = true)
    @Mapping(target = "answer", ignore = true)
    @Mapping(target = "unitScore", ignore = true)
    @Mapping(source = "questionId", target = "questionId")
    @Mapping(source = "questionContent", target = "questionContent")
    @Mapping(source = "correctAnswer", target = "correctAnswer")
    @Mapping(source = "questionType", target = "questionType")
    @Mapping(source = "options", target = "options")
    QuestionResponse toQuestionResponse(Question question);


    @Mapping(source = "questionId", target = "questionId")
    @Mapping(source = "questionContent", target = "questionContent")
    @Mapping(source = "correctAnswer", target = "correctAnswer")
    @Mapping(source = "questionType", target = "questionType")
    @Mapping(source = "options", target = "options")
    QuestionCreationResponse toQuestionCreationResponse(Question question);


    @Mapping(target = "questionId", ignore = true)
    @Mapping(target = "questionContent", source = "questionContent")
    //@Mapping(target = "correct_answer", source = "correctAnswer")
    @Mapping(target = "questionType", source = "questionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Question toQuestion(QuestionUpdateRequest questionUpdateRequest);

    @Mapping(target = "questionId", ignore = true)
    @Mapping(target = "questionContent", source = "questionContent")
    //@Mapping(target = "correct_answer", source = "correctAnswer")
    @Mapping(target = "questionType", source = "questionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Question toQuestion(QuestionCreationRequest questionCreationRequest);

    @Mapping(target = "questionId", ignore = true)
    @Mapping(target = "questionContent", source = "questionContent")
    @Mapping(target = "questionType", source = "questionType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateQuestion(@MappingTarget Question question, QuestionUpdateRequest questionUpdateRequest);



}

