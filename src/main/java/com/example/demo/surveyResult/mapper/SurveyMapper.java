package com.example.demo.surveyResult.mapper;

import com.example.demo.surveyResult.dto.SurveyResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

//
//설문 결과 저장 및 유형 정보 조회를 위한 MyBatis Mapper 인터페이스입니다.

@Mapper
public interface SurveyMapper {
    /**
     * 사용자의 설문 결과(bugId)를 Users 테이블에 저장합니다.
     * @param bugId 유형 ID
     */
    void surveyResult(@Param("userId") int userId, @Param("bugId") int bugId);
    /**
     * bugId에 해당하는 설문 결과 상세 정보를 조회합니다.
     * @param bugId 유형 ID
     * @return 설문 결과 응답 DTO
     */
    SurveyResponseDto selectedBugId(@Param("bugId") int bugId);
}