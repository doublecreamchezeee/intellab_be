package com.example.problemservice.mapper;

import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.dto.response.DefaultCode.PartialBoilerplateResponse;
import com.example.problemservice.model.DefaultCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DefaultCodeMapper {
    @Mapping(target = "language", source = "language.shortName")
    @Mapping(target = "code", source = "code")
    DefaultCodeResponse toResponse(DefaultCode defaultCode);

    @Mapping(target = "languageId", source = "language.id")
    @Mapping(target = "longName", source = "language.longName")
    @Mapping(target = "shortName", source = "language.shortName")
    @Mapping(target = "code", source = "code")
    PartialBoilerplateResponse toPartialBoilerplateResponse(DefaultCode defaultCode);
}
