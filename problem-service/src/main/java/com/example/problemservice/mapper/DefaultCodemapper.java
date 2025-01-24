package com.example.problemservice.mapper;

import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.model.DefaultCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DefaultCodemapper {
    @Mapping(target = "language", source = "language.shortName")
    @Mapping(target = "code", source = "code")
    DefaultCodeResponse toResponse(DefaultCode defaultCode);
}
