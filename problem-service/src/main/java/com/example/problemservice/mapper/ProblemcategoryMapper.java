package com.example.problemservice.mapper;

import com.example.problemservice.dto.response.Problem.CategoryResponse;
import com.example.problemservice.model.ProblemCategory;
import com.example.problemservice.model.course.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProblemcategoryMapper {

    @Mapping(target = "categoryId", source = "problemCategoryID.categoryId")
    Category toCategory(ProblemCategory problemCategory);

    @Mapping(target = "categoryId", source = "problemCategoryID.categoryId")
    CategoryResponse toCategoryResponse(ProblemCategory problemCategory);
}
