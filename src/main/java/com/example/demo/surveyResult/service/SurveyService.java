package com.example.demo.surveyResult.service;

import com.example.demo.surveyResult.dto.SurveyResponseDto;
import com.example.demo.surveyResult.dto.SurveyResultDto;
import com.example.demo.surveyResult.mapper.SurveyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 설문 결과 저장 및 유형 조회 비즈니스 로직 처리 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyMapper surveyMapper;

    /**
     * 인증된 사용자의 설문 결과를 저장합니다.
     * @param userId 로그인 사용자 ID
     * @param bugId 설문 결과로 판단된 유형 ID
     */

    public void SurveyResult(int userId, int bugId){
        surveyMapper.surveyResult(userId, bugId);
    }

    /**
     * bugId로 유형 정보를 조회합니다. 인증 불필요.
     * @param bugId 유형 ID
     * @return 유형 상세 정보 DTO
     */
    public SurveyResponseDto selectedBugId(int bugId){
        return surveyMapper.selectedBugId(bugId);
    }
}
