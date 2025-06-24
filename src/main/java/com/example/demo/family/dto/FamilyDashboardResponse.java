package com.example.demo.family.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 가족 스페이스 대시보드 응답 DTO (간소화된 식물 정보 + 추천 정보 포함)
 * 메인 대시보드 페이지에서 사용하는 핵심 정보만 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDashboardResponse {

    /**
     * 가족 스페이스 기본 정보
     */
    private FamilySpace family;

    /**
     * 가족 구성원 목록 (요금제 정보 포함)
     */
    private List<FamilyMember> members;

    /**
     * 할인 정보
     */
    private DiscountInfo discount;

    /**
     * 간소화된 식물 정보 (레벨, 종류, 생성여부만)
     */
    private PlantInfo plant;

    /**
     * 추천 정보 요약 (선택적)
     */
    private RecommendationSummary recommendationSummary;

    /**
     * 기존 4개 매개변수 생성자 (하위 호환성 유지)
     */
    public FamilyDashboardResponse(FamilySpace family, List<FamilyMember> members, 
                                 DiscountInfo discount, PlantInfo plant) {
        this.family = family;
        this.members = members;
        this.discount = discount;
        this.plant = plant;
        this.recommendationSummary = null; // 추천 정보 없음
    }

    /**
     * 편의 메서드: 총 구성원 수
     */
    public int getTotalMembers() {
        return members != null ? members.size() : 0;
    }

    /**
     * 편의 메서드: 요금제 가입 구성원 수
     */
    public long getMembersWithPlan() {
        return members != null ? members.stream().filter(FamilyMember::hasPlan).count() : 0;
    }

    /**
     * 편의 메서드: 설문조사 완료 구성원 수
     */
    public long getMembersWithSurvey() {
        return members != null ? members.stream().filter(FamilyMember::hasSurvey).count() : 0;
    }

    /**
     * 편의 메서드: 버그 매핑 완료 구성원 수
     */
    public long getMembersWithBugMapping() {
        return members != null ? members.stream().filter(FamilyMember::hasBugMapping).count() : 0;
    }

    /**
     * 편의 메서드: 설문조사 완료율 (백분율)
     */
    public int getSurveyCompletionRate() {
        if (members == null || members.isEmpty()) {
            return 0;
        }
        return (int) Math.round((double) getMembersWithSurvey() / getTotalMembers() * 100);
    }

    /**
     * 편의 메서드: 가족 추천을 위한 준비 상태 확인
     * @return 모든 구성원이 설문조사를 완료했으면 true
     */
    public boolean isReadyForRecommendation() {
        return getTotalMembers() > 0 && getMembersWithSurvey() == getTotalMembers();
    }

    /**
     * 추천 정보 요약 내부 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationSummary {
        /**
         * 추천 가능 여부
         */
        private boolean available;

        /**
         * 최고 추천 요금제 (1순위)
         */
        private TopPlan topPlan;

        /**
         * 결합 상품 추천
         */
        private CombinationInfo combinationInfo;

        /**
         * 상태 메시지
         */
        private String statusMessage;

        /**
         * 가족 유형
         */
        private String familyType;

        /**
         * 추천 불가능한 경우 생성
         */
        public static RecommendationSummary unavailable(String reason) {
            RecommendationSummary summary = new RecommendationSummary();
            summary.available = false;
            summary.statusMessage = reason;
            summary.topPlan = null;
            summary.combinationInfo = null;
            summary.familyType = null;
            return summary;
        }

        /**
         * 추천 가능한 경우 생성
         */
        public static RecommendationSummary available(TopPlan topPlan, CombinationInfo combinationInfo, 
                                                    String familyType, String statusMessage) {
            RecommendationSummary summary = new RecommendationSummary();
            summary.available = true;
            summary.topPlan = topPlan;
            summary.combinationInfo = combinationInfo;
            summary.familyType = familyType;
            summary.statusMessage = statusMessage;
            return summary;
        }
    }

    /**
     * 최고 추천 요금제 (간소화)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPlan {
        private String planName;
        private Integer originalPrice;
        private Integer discountPrice;
        private String shortReason;
    }

    /**
     * 결합 상품 정보 (간소화)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CombinationInfo {
        private String combinationName;
        private Integer monthlySavings;
        private String highlight; // "최대 XX원 절약!" 형태
    }
}