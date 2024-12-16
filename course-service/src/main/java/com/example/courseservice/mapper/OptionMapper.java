package com.example.courseservice.mapper;


import com.example.courseservice.dto.request.Option.OptionRequest;
import com.example.courseservice.dto.response.Option.OptionResponse;
import com.example.courseservice.model.Option;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OptionMapper {

    @Mapping(target = "order",source = "option_id.option_order")
    @Mapping(target = "content",source = "content")
    OptionResponse toResponse(Option option);

    @Mapping(target = "option_id.question_id", ignore = true)
    @Mapping(target = "option_id.option_order", source = "order")
    @Mapping(target = "content", source = "content")
    Option toOption(OptionRequest optionRequest);

    @Mapping(target = "option_id.question_id", ignore = true)
    @Mapping(target = "option_id.option_order", ignore = true)
    @Mapping(target = "content", source = "content")
    void update(@MappingTarget Option option, OptionRequest optionRequest);

}
