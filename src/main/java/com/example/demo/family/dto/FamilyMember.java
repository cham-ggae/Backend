package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 가족 구성원 정보 DTO
 * Users 테이블과 Plans 테이블을 LEFT JOIN한 결과와 매핑
 * 가족 스페이스 대시보드에서 구성원 정보 표시에 사용
 * Long 타입으로 통일
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember {

    /**
     * 사용자 고유 ID (Primary Key)
     * Users.uid와 매핑
     */
    private Long uid;

    /**
     * 가족 스페이스 ID
     * Users.fid와 매핑
     */
    private Long fid;

    /**
     * 사용자 이름
     * 카카오 로그인 시 제공되는 이름 또는 사용자 입력 이름
     * 최대 15자까지 저장 가능
     */
    private String name;

    /**
     * 사용자 이메일
     * 카카오 로그인 시 제공되는 이메일 또는 사용자 입력 이메일
     */
    private String email;

    /**
     * 사용자 나이
     * 카카오 측으로 부터 받아옴 (동의 항목)
     * 20~29 형태의 문자열
     * NULL 가능 (선택적 입력)
     */
    private String age;

    /**
     * 서비스 가입일시
     * 장기 고객 할인 계산 기준일
     * 자동으로 현재 시간이 설정됨
     */
    private LocalDateTime joinDate;

    /**
     * 프로필 이미지 URL
     * 카카오 로그인에서 제공되는 프로필 이미지
     */
    private String profileImage;

    // 설문조사 관련 정보
    /**
     * 매칭된 버그 ID (설문조사 결과)
     * 설문조사 결과에 따라 매핑되는 버그 유형 ID
     * NULL 가능 (설문조사 미완료 시)
     */
    private Integer bugId;

    /**
     * 설문조사 실시 날짜
     * 설문조사를 완료한 날짜
     * NULL 가능 (설문조사 미완료 시)
     */
    private LocalDateTime surveyDate;

    // 요금제 정보 (Plans 테이블 - LEFT JOIN)
    /**
     * 현재 사용 중인 요금제 ID
     * Plans.plan_id와 매핑
     * NULL인 경우 요금제 미가입 상태
     */
    private Integer planId;

    /**
     * 요금제 이름
     * 예: "5G 시그니처", "5G 스탠다드", "5G 프리미어" 등
     * NULL인 경우 요금제 미가입 상태
     */
    private String planName;

    /**
     * 요금제 월 이용료 (원)
     * 할인 전 기본 요금
     * NULL인 경우 요금제 미가입 상태
     */
    private Integer price;

    /**
     * 요금제 혜택 설명
     * 데이터, 통화, 문자 등의 제공 내용
     * 최대 150자까지 저장 가능
     */
    private String benefit;

    // UI 표시용 확장 데이터
    /**
     * 현재 데이터 사용량 표시용
     * 예: "45GB", "23GB", "무제한" 등
     * 실제 사용량은 외부 API에서 조회하거나 하드코딩
     * UI에서 시각적 표시를 위해 사용
     */
    private String dataUsage;

    // 추천 요금제 정보 (설문조사 결과 기반)
    /**
     * 설문조사 결과 기반 추천 요금제 목록
     * suggest1, suggest2에 해당하는 요금제 정보
     */
    private List<RecommendedPlan> recommendedPlans;

    /**
     * 추천 요금제 정보 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedPlan {
        /**
         * 추천 순위 (1: suggest1, 2: suggest2)
         */
        private Integer rank;
        
        /**
         * 요금제 ID
         */
        private Integer planId;
        
        /**
         * 요금제명
         */
        private String planName;
        
        /**
         * 기본 가격
         */
        private Integer price;
        
        /**
         * 할인 가격
         */
        private Integer discountPrice;
        
        /**
         * 요금제 혜택
         */
        private String benefit;
        
        /**
         * 요금제 링크
         */
        private String link;
    }

    // 생성자들
    public FamilyMember(Long uid, Long fid, String name, String email, String age, LocalDateTime joinDate) {
        this.uid = uid;
        this.fid = fid;
        this.name = name;
        this.email = email;
        this.age = age;
        this.joinDate = joinDate;
    }

    /**
     * 요금제 가입 여부 확인
     * @return 요금제 ID와 이름이 모두 있으면 true, 그렇지 않으면 false
     */
    public boolean hasPlan() {
        return planId != null && planName != null;
    }

    /**
     * 설문조사 완료 여부 확인
     * @return 설문조사 날짜가 있으면 true, 그렇지 않으면 false
     */
    public boolean hasSurvey() {
        return surveyDate != null;
    }

    /**
     * 버그 정보 매핑 여부 확인
     * @return 버그 ID가 있으면 true, 그렇지 않으면 false
     */
    public boolean hasBugMapping() {
        return bugId != null;
    }

    /**
     * 요금제 요약 정보 생성 (UI 표시용)
     * @return 요금제명과 월 이용료를 포함한 요약 문자열
     */
    public String getPlanSummary() {
        if (!hasPlan()) {
            return "요금제 없음";
        }
        return planName + " (월 " + String.format("%,d", price) + "원)";
    }

    /**
     * 설문조사 상태 요약 정보 생성 (UI 표시용)
     * @return 설문조사 완료 여부와 버그 매핑 상태를 포함한 요약 문자열
     */
    public String getSurveyStatusSummary() {
        if (!hasSurvey()) {
            return "설문조사 미완료";
        }
        if (hasBugMapping()) {
            return "설문조사 완료 (버그 ID: " + bugId + ")";
        }
        return "설문조사 완료";
    }
}