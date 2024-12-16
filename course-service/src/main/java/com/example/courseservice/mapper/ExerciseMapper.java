package com.example.courseservice.mapper;

import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.response.exercise.ExerciseDetailResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = QuestionMapper.class)
public interface ExerciseMapper {

    @Mapping(source = "exercise_id", target = "exerciseId")
    @Mapping(source = "exercise_name", target = "exerciseName")
    @Mapping(source = "description", target = "exerciseDescription")
    //@Mapping(source = "questionList", target = "questionList")
    ExerciseResponse toExerciseResponse(Exercise exercise);

    @Mapping(target = "exercise_id", ignore = true)
    @Mapping(target = "exercise_name", source = "name")
    @Mapping(target = "description", source = "description")
    Exercise toExercise(ExerciseCreationRequest exerciseCreationRequest);




}

//UUID exercise_id;
//String exercise_name;

//UUID exerciseId;
//String exerciseName;
//String exerciseDescription;
//List<Question> questionList;
