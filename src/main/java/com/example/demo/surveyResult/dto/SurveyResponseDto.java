package com.example.demo.surveyResult.dto;

import lombok.Data;
/**
 * 설문 결과 조회 응답을 위한 DTO입니다.
 * bugId를 기반으로 조회된 유형 상세 정보를 포함합니다.
 */
@Data
public class SurveyResponseDto {
    private int bugId;
    private int suggest1;
    private int suggest2;
    private String bugName;
    private String feature;
    private String personality;
}
