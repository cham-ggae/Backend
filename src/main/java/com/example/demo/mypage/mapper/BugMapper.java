package com.example.demo.mypage.mapper;

import com.example.demo.surveyResult.dto.SurveyResponseDto;
import com.example.demo.surveyResult.dto.SurveyResultDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
/**
 * BugMapper 클래스입니다.
 */
public interface BugMapper {

    @Select("SELECT bug_name, feature, personality, suggest1, suggest2 FROM Bugs WHERE bug_id = #{bugId}")
    @Results({
            @Result(property = "bugName", column = "bug_name"),
            @Result(property = "feature", column = "feature"),
            @Result(property = "personality", column = "personality"),
            @Result(property = "suggest1", column = "suggest1"),
            @Result(property = "suggest2", column = "suggest2")
    })
    SurveyResponseDto findBugInfoById(Long bugId);}

