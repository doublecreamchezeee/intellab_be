package com.example.courseservice.mapper;

import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.model.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = QuestionMapper.class)
public interface ExerciseMapper {
    @Mapping(target = "exerciseId",source = "exerciseId")
    @Mapping(target = "questionsPerExercise",source = "questionsPerExercise")
    @Mapping(target = "passingQuestions",source = "passingQuestions")
    @Mapping(target = "questionList",source = "questionList")
    ExerciseResponse toExerciseResponse(Exercise exercise);

}

//UUID exercise_id;
//String exercise_name;

//UUID quizId;
//String exerciseName;
//String exerciseDescription;
//List<Question> questionList;
