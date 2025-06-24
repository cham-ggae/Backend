package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 가족 구성원 설문 정보 DTO
 * 요금제 추천을 위한 구성원별 설문 결과 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberSurveyInfo {

    /**
     * 사용자 ID
     */
    private Long uid;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 나이
     */
    private String age;

    /**
     * 성별
     */
    private String gender;

    /**
     * 설문 결과 유형 ID
     */
    private Long bugId;

    /**
     * 설문 결과 유형명
     */
    private String bugName;

    /**
     * 설문 결과 특성
     */
    private String feature;

    /**
     * 설문 결과 성격
     */
    private String personality;

    /**
     * 추천 요금제 1
     */
    private Integer suggest1;

    /**
     * 추천 요금제 2
     */
    private Integer suggest2;

    /**
     * 현재 사용 중인 요금제 ID
     */
    private Integer currentPlanId;

    /**
     * 설문 완료 여부
     */
    public boolean hasSurveyResult() {
        return bugId != null;
    }
} 