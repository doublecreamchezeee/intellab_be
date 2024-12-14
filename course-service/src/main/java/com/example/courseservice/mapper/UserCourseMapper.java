package com.example.courseservice.mapper;

import com.example.courseservice.dto.response.userCourses.UserCoursesResponse;
import com.example.courseservice.model.UserCourses;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserCourseMapper {

    UserCoursesResponse toUserCoursesResponse(UserCourses userCourses);
}
