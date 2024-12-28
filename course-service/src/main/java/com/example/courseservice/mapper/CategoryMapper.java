package com.example.courseservice.mapper;


import com.example.courseservice.dto.response.category.CategoryResponse;
import com.example.courseservice.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "name", source = "name")
    CategoryResponse categoryToCategoryResponse(Category category);
}
