package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Option.OptionRequest;
import com.example.courseservice.dto.response.Option.OptionResponse;
import com.example.courseservice.model.Option;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OptionMapper {

    @Mapping(target = "order",source = "optionId.optionOrder")
    @Mapping(target = "content",source = "content")
    OptionResponse toResponse(Option option);

    @Mapping(target = "optionId.questionId", ignore = true)
    @Mapping(target = "optionId.optionOrder", source = "order")
    @Mapping(target = "content", source = "content")
    Option toOption(OptionRequest optionRequest);

    @Mapping(target = "optionId.questionId", ignore = true)
    @Mapping(target = "optionId.optionOrder", ignore = true)
    @Mapping(target = "content", source = "content")
    void update(@MappingTarget Option option, OptionRequest optionRequest);

}
