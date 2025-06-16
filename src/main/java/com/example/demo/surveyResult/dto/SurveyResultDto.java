package com.example.demo.surveyResult.dto;

import lombok.Data;
/**
 * 설문 결과 저장 시 클라이언트로부터 전달받는 DTO.
 * 인증된 사용자의 userId는 백엔드에서 주입하므로 포함하지 않음.
 */
@Data
public class SurveyResultDto {
    private int bugId;
}

