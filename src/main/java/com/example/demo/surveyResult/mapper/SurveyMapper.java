package com.example.demo.surveyResult.mapper;

import com.example.demo.surveyResult.dto.SurveyResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SurveyMapper {
    void surveyResult(@Param("userId") int userId, @Param("bugId") int bugId);

    SurveyResponseDto selectedBugId(@Param("bugId") int bugId);
}