package com.example.courseservice.service;

import com.example.courseservice.dto.request.Option.OptionRequest;
import com.example.courseservice.dto.response.Option.OptionResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.OptionMapper;
import com.example.courseservice.model.Option;
import com.example.courseservice.model.compositeKey.OptionID;
import com.example.courseservice.repository.OptionRepository;
import com.example.courseservice.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionService {
    OptionRepository optionRepository;
    OptionMapper optionMapper;
    QuestionRepository questionRepository;

//    public List<OptionResponse> getOptionByQuestionId(UUID questionId) {
//        return optionRepository.findByQuestion_id(questionId).stream().map(optionMapper::toResponse).toList();
//    }



    public OptionResponse updateOption(UUID questionID, OptionRequest request) {
        if(!questionRepository.existsById(questionID)) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        OptionID optionID = new OptionID(questionID, request.getOrder());
        if(!optionRepository.existsById(optionID)) {
            throw new AppException(ErrorCode.OPTION_NOT_FOUND);
        }
        Option option = optionRepository.findById(optionID).get();

        optionMapper.update(option, request);


        option = optionRepository.save(option);
        return optionMapper.toResponse(option);
    }

    public void deleteOption(UUID questionID, Integer optionOrder) {
        if(!questionRepository.existsById(questionID)) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        OptionID Id = new OptionID(questionID, optionOrder);
        optionRepository.deleteById(Id);
    }

    public OptionResponse creteOption(UUID questionID, OptionRequest request) {
        if(!questionRepository.existsById(questionID)) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        Option option = optionMapper.toOption(request);
        option = optionRepository.save(option);
        return optionMapper.toResponse(option);
    }



}
