package com.example.demo.surveyResult.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SurveyMapper {
    void surveyResult(@Param("userId") int userId, @Param("bugId") int bugId);}