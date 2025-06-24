package com.example.demo.family.dto;

import com.example.demo.mypage.dto.RecommendHistoryData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 가족 요금제 추천 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyPlanRecommendationResponse {

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 가족 기본 정보
     */
    private FamilySpace family;

    /**
     * 가족 구성원 설문 정보 목록
     */
    private List<FamilyMemberSurveyInfo> memberSurveyInfos;

    /**
     * 추천 요금제 목록 (우선순위 순)
     */
    private List<RecommendedPlan> recommendedPlans;

    /**
     * 추천 근거 정보
     */
    private RecommendationReason recommendationReason;

    /**
     * 결합 상품 추천 정보
     */
    private CombinationRecommendation combinationRecommendation;

    /**
     * 성공 응답 생성
     */
    public static FamilyPlanRecommendationResponse success(
            FamilySpace family,
            List<FamilyMemberSurveyInfo> memberSurveyInfos,
            List<RecommendedPlan> recommendedPlans,
            RecommendationReason recommendationReason,
            CombinationRecommendation combinationRecommendation) {
        FamilyPlanRecommendationResponse response = new FamilyPlanRecommendationResponse();
        response.success = true;
        response.message = "가족 요금제 추천이 완료되었습니다.";
        response.family = family;
        response.memberSurveyInfos = memberSurveyInfos;
        response.recommendedPlans = recommendedPlans;
        response.recommendationReason = recommendationReason;
        response.combinationRecommendation = combinationRecommendation;
        return response;
    }

    /**
     * 실패 응답 생성
     */
    public static FamilyPlanRecommendationResponse failure(String message) {
        FamilyPlanRecommendationResponse response = new FamilyPlanRecommendationResponse();
        response.success = false;
        response.message = message;
        response.family = null;
        response.memberSurveyInfos = null;
        response.recommendedPlans = null;
        response.recommendationReason = null;
        response.combinationRecommendation = null;
        return response;
    }

    /**
     * 추천 요금제 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedPlan {
        private Integer planId;
        private String planName;
        private Integer price;
        private Integer discountPrice;
        private String benefit;
        private String link;
        private Double score; // 추천 점수
        private String reason; // 추천 이유
    }

    /**
     * 추천 근거 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationReason {
        private int totalMembers; // 총 구성원 수
        private int membersWithSurvey; // 설문 완료 구성원 수
        private List<String> dominantFeatures; // 주요 특성들
        private String familyType; // 가족 유형 (예: "데이터 중심형", "통화 중심형" 등)
        private String recommendationSummary; // 추천 요약
    }

    public CombinationRecommendation getCombinationRecommendation() {
        return combinationRecommendation;
    }

    public void setCombinationRecommendation(CombinationRecommendation combinationRecommendation) {
        this.combinationRecommendation = combinationRecommendation;
    }

    /**
     * 결합 상품 추천 정보
     */
    public static class CombinationRecommendation {
        private String recommendedCombination;     // 추천 결합 상품명
        private int totalMonthlySavings;          // 총 월 절약 금액
        private String savingsBreakdown;          // 절약 내역 상세
        private String discountRate;              // 할인율
        private String recommendationReason;      // 추천 이유
        private List<CombinationDetail> availableCombinations; // 이용 가능한 결합 상품들

        public CombinationRecommendation() {}

        public CombinationRecommendation(String recommendedCombination, int totalMonthlySavings, 
                                       String savingsBreakdown, String discountRate, 
                                       String recommendationReason, List<CombinationDetail> availableCombinations) {
            this.recommendedCombination = recommendedCombination;
            this.totalMonthlySavings = totalMonthlySavings;
            this.savingsBreakdown = savingsBreakdown;
            this.discountRate = discountRate;
            this.recommendationReason = recommendationReason;
            this.availableCombinations = availableCombinations;
        }

        // getters and setters
        public String getRecommendedCombination() { return recommendedCombination; }
        public void setRecommendedCombination(String recommendedCombination) { this.recommendedCombination = recommendedCombination; }

        public int getTotalMonthlySavings() { return totalMonthlySavings; }
        public void setTotalMonthlySavings(int totalMonthlySavings) { this.totalMonthlySavings = totalMonthlySavings; }

        public String getSavingsBreakdown() { return savingsBreakdown; }
        public void setSavingsBreakdown(String savingsBreakdown) { this.savingsBreakdown = savingsBreakdown; }

        public String getDiscountRate() { return discountRate; }
        public void setDiscountRate(String discountRate) { this.discountRate = discountRate; }

        public String getRecommendationReason() { return recommendationReason; }
        public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

        public List<CombinationDetail> getAvailableCombinations() { return availableCombinations; }
        public void setAvailableCombinations(List<CombinationDetail> availableCombinations) { this.availableCombinations = availableCombinations; }
    }

    /**
     * 결합 상품 상세 정보
     */
    public static class CombinationDetail {
        private String combinationName;           // 결합 상품명
        private String description;               // 설명
        private int monthlySavings;              // 월 절약 금액
        private String conditions;               // 결합 조건
        private boolean isRecommended;           // 추천 여부

        public CombinationDetail() {}

        public CombinationDetail(String combinationName, String description, int monthlySavings, 
                               String conditions, boolean isRecommended) {
            this.combinationName = combinationName;
            this.description = description;
            this.monthlySavings = monthlySavings;
            this.conditions = conditions;
            this.isRecommended = isRecommended;
        }

        // getters and setters
        public String getCombinationName() { return combinationName; }
        public void setCombinationName(String combinationName) { this.combinationName = combinationName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getMonthlySavings() { return monthlySavings; }
        public void setMonthlySavings(int monthlySavings) { this.monthlySavings = monthlySavings; }

        public String getConditions() { return conditions; }
        public void setConditions(String conditions) { this.conditions = conditions; }

        public boolean isRecommended() { return isRecommended; }
        public void setRecommended(boolean recommended) { isRecommended = recommended; }
    }
} 