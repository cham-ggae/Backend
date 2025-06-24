package com.example.demo.family.service;

import com.example.demo.family.dao.FamilyDao;
import com.example.demo.family.dto.*;
import com.example.demo.mypage.dto.RecommendHistoryData;
import com.example.demo.mypage.mapper.PlanMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 가족 요금제 추천 서비스
 * 가족 구성원들의 설문 결과를 종합하여 최적의 요금제를 추천
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FamilyPlanRecommendationService {

    @Autowired
    private FamilyDao familyDao;

    @Autowired
    private PlanMapper planMapper;

    /**
     * 가족 요금제 추천
     *
     * @param fid 가족 스페이스 ID
     * @param uid 요청자 사용자 ID (권한 체크용)
     * @return 추천 결과
     */
    public FamilyPlanRecommendationResponse recommendFamilyPlans(Long fid, Long uid) {
        try {
            // 1. 권한 체크
            if (!familyDao.isUserFamilyMember(uid, fid)) {
                return FamilyPlanRecommendationResponse.failure("해당 가족의 구성원이 아닙니다.");
            }

            // 2. 가족 기본 정보 조회
            FamilySpace family = familyDao.getFamilySpaceById(fid);
            if (family == null) {
                return FamilyPlanRecommendationResponse.failure("존재하지 않는 가족 스페이스입니다.");
            }

            // 3. 가족 구성원 설문 정보 조회
            List<FamilyMemberSurveyInfo> memberSurveyInfos = familyDao.getFamilyMembersSurveyInfo(fid);
            if (memberSurveyInfos.isEmpty()) {
                return FamilyPlanRecommendationResponse.failure("가족 구성원 정보를 찾을 수 없습니다.");
            }

            // 4. 설문 완료 구성원 필터링
            List<FamilyMemberSurveyInfo> membersWithSurvey = memberSurveyInfos.stream()
                    .filter(FamilyMemberSurveyInfo::hasSurveyResult)
                    .collect(Collectors.toList());

            // 5. 설문 완료 구성원이 없는 경우에만 에러 반환
            if (membersWithSurvey.isEmpty()) {
                return FamilyPlanRecommendationResponse.failure("가족 구성원 모두 설문을 완료해주세요. 설문 결과를 바탕으로 맞춤 추천을 제공합니다.");
            }

            // 6. 설문 미완료 구성원에 대한 안내 메시지 생성
            int totalMembers = memberSurveyInfos.size();
            int membersWithSurveyCount = membersWithSurvey.size();
            String recommendationMessage = "가족 요금제 추천이 완료되었습니다.";
            
            if (membersWithSurveyCount < totalMembers) {
                int incompleteSurveys = totalMembers - membersWithSurveyCount;
                recommendationMessage += String.format(" (설문 미완료 %d명 - 모든 구성원이 설문을 완료하면 더 정확한 추천을 받을 수 있습니다.)", incompleteSurveys);
            }

            // 7. 요금제 추천 알고리즘 실행
            List<FamilyPlanRecommendationResponse.RecommendedPlan> recommendedPlans = 
                    calculateRecommendedPlans(membersWithSurvey);

            // 8. 추천 근거 생성
            FamilyPlanRecommendationResponse.RecommendationReason reason = 
                    generateRecommendationReason(memberSurveyInfos, membersWithSurvey);

            // 9. 결합 상품 추천 생성 (전체 가족 구성원 기준)
            FamilyPlanRecommendationResponse.CombinationRecommendation combinationRecommendation = 
                    calculateCombinationRecommendation(memberSurveyInfos, membersWithSurvey);

            // 10. 응답 생성 시 맞춤 메시지 적용
            FamilyPlanRecommendationResponse response = FamilyPlanRecommendationResponse.success(
                    family,
                    memberSurveyInfos,
                    recommendedPlans,
                    reason,
                    combinationRecommendation
            );
            response.setMessage(recommendationMessage);
            
            return response;

        } catch (Exception e) {
            log.error("가족 요금제 추천 중 오류 발생: fid={}, uid={}, error={}", fid, uid, e.getMessage(), e);
            return FamilyPlanRecommendationResponse.failure("요금제 추천 중 오류가 발생했습니다.");
        }
    }

    /**
     * 요금제 추천 알고리즘
     */
    private List<FamilyPlanRecommendationResponse.RecommendedPlan> calculateRecommendedPlans(
            List<FamilyMemberSurveyInfo> membersWithSurvey) {

        // 1. 모든 구성원의 추천 요금제 수집 및 가중치 계산
        Map<Integer, PlanScore> planScores = new HashMap<>();
        
        for (FamilyMemberSurveyInfo member : membersWithSurvey) {
            if (member.getSuggest1() != null) {
                PlanScore score = planScores.getOrDefault(member.getSuggest1(), new PlanScore());
                score.addScore(3.0); // 첫 번째 추천에 높은 가중치
                score.addMember(member);
                planScores.put(member.getSuggest1(), score);
            }
            if (member.getSuggest2() != null) {
                PlanScore score = planScores.getOrDefault(member.getSuggest2(), new PlanScore());
                score.addScore(1.5); // 두 번째 추천에 낮은 가중치
                score.addMember(member);
                planScores.put(member.getSuggest2(), score);
            }
        }

        // 2. 가족 특성 분석
        FamilyCharacteristics familyChar = analyzeFamilyCharacteristics(membersWithSurvey);

        // 3. 각 요금제에 대해 상세 정보 조회 및 점수 보정
        List<FamilyPlanRecommendationResponse.RecommendedPlan> recommendedPlans = new ArrayList<>();
        
        for (Map.Entry<Integer, PlanScore> entry : planScores.entrySet()) {
            RecommendHistoryData planData = planMapper.findPlanById(entry.getKey());
            if (planData != null) {
                // 기본 점수 계산
                double baseScore = entry.getValue().getTotalScore();
                
                // 가족 특성에 따른 점수 보정
                double adjustedScore = adjustScoreByFamilyCharacteristics(
                    planData, baseScore, familyChar, membersWithSurvey.size());
                
                // 추천 이유 생성
                String reason = generateDetailedPlanReason(
                    planData, entry.getValue(), familyChar, membersWithSurvey.size());

                FamilyPlanRecommendationResponse.RecommendedPlan recommendedPlan = 
                        new FamilyPlanRecommendationResponse.RecommendedPlan(
                                planData.getPlanId(),
                                planData.getPlanName(),
                                planData.getPrice(),
                                planData.getDiscountPrice(),
                                planData.getBenefit(),
                                planData.getLink(),
                                Math.round(adjustedScore * 100.0) / 100.0, // 소수점 2자리
                                reason
                        );
                recommendedPlans.add(recommendedPlan);
            }
        }

        // 4. 점수순으로 정렬하여 상위 3개 반환
        return recommendedPlans.stream()
                .sorted((p1, p2) -> Double.compare(p2.getScore(), p1.getScore()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 가족 특성 분석
     */
    private FamilyCharacteristics analyzeFamilyCharacteristics(List<FamilyMemberSurveyInfo> members) {
        FamilyCharacteristics characteristics = new FamilyCharacteristics();
        
        // 연령대 분석
        Map<String, Integer> ageGroups = new HashMap<>();
        for (FamilyMemberSurveyInfo member : members) {
            if (member.getAge() != null) {
                ageGroups.put(member.getAge(), ageGroups.getOrDefault(member.getAge(), 0) + 1);
            }
        }
        
        // 특성 분석
        Map<String, Integer> features = new HashMap<>();
        Map<String, Integer> personalities = new HashMap<>();
        
        for (FamilyMemberSurveyInfo member : members) {
            if (member.getFeature() != null) {
                features.put(member.getFeature(), features.getOrDefault(member.getFeature(), 0) + 1);
            }
            if (member.getPersonality() != null) {
                personalities.put(member.getPersonality(), personalities.getOrDefault(member.getPersonality(), 0) + 1);
            }
        }
        
        characteristics.setDominantAgeGroup(findDominantKey(ageGroups));
        characteristics.setDominantFeature(findDominantKey(features));
        characteristics.setDominantPersonality(findDominantKey(personalities));
        characteristics.setMemberCount(members.size());
        
        return characteristics;
    }

    /**
     * 가족 특성에 따른 점수 보정
     */
    private double adjustScoreByFamilyCharacteristics(
            RecommendHistoryData planData, 
            double baseScore, 
            FamilyCharacteristics familyChar, 
            int memberCount) {
        
        double adjustedScore = baseScore;
        
        // 1. 실제 LG U+ 요금제별 특성 점수 보정
        switch (planData.getPlanId()) {
            case 1: // 5G 프리미어 에센셜 (85,000원 → 58,500원, 할인율 31%)
                // 프리미어 서비스 + 합리적 가격 - 안정성과 가성비 모두 중시
                if (isStabilityOriented(familyChar)) {
                    adjustedScore += 1.2;
                }
                if (isCostEffective(familyChar)) {
                    adjustedScore += 0.8;
                }
                break;
                
            case 2: // 5G 스탠다드 (75,000원 → 56,250원, 할인율 25%)
                // 중간 가격대 + 기본 혜택 - 가성비 최우선
                if (isCostEffective(familyChar)) {
                    adjustedScore += 1.5;
                }
                if (isStabilityOriented(familyChar)) {
                    adjustedScore += 0.7;
                }
                break;
                
            case 3: // 5G 심플+ (61,000원 → 45,750원, 할인율 25%)
                // 최저 가격 - 경제성 최우선
                if (isEconomyOriented(familyChar)) {
                    adjustedScore += 2.0;
                }
                if (isCostEffective(familyChar)) {
                    adjustedScore += 1.0;
                }
                // 대가족일수록 경제적 요금제 선호
                if (memberCount >= 4) {
                    adjustedScore += 1.2;
                }
                break;
                
            case 4: // 5G 프리미어 레귤러 (95,000원 → 66,000원, 할인율 31%)
                // 최고 가격 + 최대 혜택 - 프리미엄 서비스 중시
                if (isPremiumOriented(familyChar)) {
                    adjustedScore += 1.8;
                }
                // 소가족일수록 프리미엄 요금제 고려 가능
                if (memberCount <= 2) {
                    adjustedScore += 0.8;
                }
                // 미디어 혜택이 중요한 가족
                if (familyChar.getDominantFeature() != null && 
                    familyChar.getDominantFeature().contains("엔터테인먼트")) {
                    adjustedScore += 1.0;
                }
                break;
        }
        
        // 2. 가족 구성원 수에 따른 보정
        if (memberCount >= 4) {
            // 대가족의 경우 경제적인 요금제 선호
            if (planData.getPlanId() == 3) { // 5G 심플+
                adjustedScore += 0.8;
            }
        } else if (memberCount <= 2) {
            // 소가족의 경우 프리미엄 요금제도 고려
            if (planData.getPlanId() == 4) { // 5G 프리미어 레귤러
                adjustedScore += 0.5;
            }
        }
        
        // 3. 할인율 고려
        double discountRate = (double)(planData.getPrice() - planData.getDiscountPrice()) / planData.getPrice();
        adjustedScore += discountRate * 0.5; // 할인율이 높을수록 점수 가산
        
        return Math.max(adjustedScore, 0.1); // 최소 점수 보장
    }

    /**
     * 상세한 추천 이유 생성
     */
    private String generateDetailedPlanReason(
            RecommendHistoryData planData, 
            PlanScore planScore, 
            FamilyCharacteristics familyChar, 
            int totalMembers) {
        
        StringBuilder reason = new StringBuilder();
        
        // 기본 선호도
        double preferenceRate = (planScore.getTotalScore() / (totalMembers * 3)) * 100;
        reason.append(String.format("가족 구성원 중 %.0f%%가 선호하는 요금제입니다. ", preferenceRate));
        
        // 실제 LG U+ 요금제별 특화 이유
        switch (planData.getPlanId()) {
            case 1: // 5G 프리미어 에센셜 (85,000원 → 58,500원)
                reason.append("무제한 통화와 U+ 모바일TV 기본 서비스가 포함된 프리미어 요금제입니다. ");
                reason.append("안정적인 5G 서비스를 합리적인 가격에 이용하고 싶은 가족에게 최적입니다.");
                break;
                
            case 2: // 5G 스탠다드 (75,000원 → 56,250원)
                reason.append("필수 통신 서비스와 U+ 모바일TV를 포함하면서도 경제적인 요금제입니다. ");
                reason.append("가성비를 중시하면서도 기본 혜택은 놓치고 싶지 않은 가족에게 추천합니다.");
                break;
                
            case 3: // 5G 심플+ (61,000원 → 45,750원)
                reason.append("기본적인 5G 통신 서비스에 집중한 가장 경제적인 요금제입니다. ");
                reason.append("통신비 절약이 최우선인 가족이나 단순한 서비스를 선호하는 가족에게 적합합니다.");
                break;
                
            case 4: // 5G 프리미어 레귤러 (95,000원 → 66,000원)
                reason.append("콘텐츠·음악 혜택(최대 9,900원)과 스마트기기 할인(최대 11,000원)이 포함된 프리미엄 요금제입니다. ");
                reason.append("다양한 디지털 서비스를 적극 활용하는 가족에게 최고의 선택입니다.");
                break;
                
            default:
                reason.append("가족의 통신 패턴에 적합한 요금제입니다.");
                break;
        }
        
        // 할인 혜택 강조
        int discountAmount = planData.getPrice() - planData.getDiscountPrice();
        if (discountAmount > 0) {
            reason.append(String.format(" 현재 월 %,d원 할인 혜택도 받을 수 있습니다.", discountAmount));
        }
        
        return reason.toString();
    }

    // 가족 특성 판단 헬퍼 메서드들
    private boolean isStabilityOriented(FamilyCharacteristics familyChar) {
        return familyChar.getDominantPersonality() != null && 
                (familyChar.getDominantPersonality().contains("안정") || 
                familyChar.getDominantPersonality().contains("신중"));
    }
    
    private boolean isCostEffective(FamilyCharacteristics familyChar) {
        return familyChar.getDominantPersonality() != null && 
                (familyChar.getDominantPersonality().contains("실용") || 
                familyChar.getDominantPersonality().contains("합리"));
    }
    
    private boolean isEconomyOriented(FamilyCharacteristics familyChar) {
        return familyChar.getDominantPersonality() != null && 
               (familyChar.getDominantPersonality().contains("절약") || 
                familyChar.getDominantPersonality().contains("경제"));
    }
    
    private boolean isPremiumOriented(FamilyCharacteristics familyChar) {
        return familyChar.getDominantFeature() != null && 
                (familyChar.getDominantFeature().contains("프리미엄") || 
                familyChar.getDominantFeature().contains("고급") ||
                familyChar.getDominantFeature().contains("미디어"));
    }

    private String findDominantKey(Map<String, Integer> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // 내부 클래스들
    private static class PlanScore {
        private double totalScore = 0.0;
        private List<FamilyMemberSurveyInfo> members = new ArrayList<>();
        
        public void addScore(double score) {
            this.totalScore += score;
        }
        
        public void addMember(FamilyMemberSurveyInfo member) {
            if (!members.contains(member)) {
                members.add(member);
            }
        }
        
        public double getTotalScore() {
            return totalScore;
        }
        
        public List<FamilyMemberSurveyInfo> getMembers() {
            return members;
        }
    }
    
    private static class FamilyCharacteristics {
        private String dominantAgeGroup;
        private String dominantFeature;
        private String dominantPersonality;
        private int memberCount;
        
        // getters and setters
        public String getDominantAgeGroup() { return dominantAgeGroup; }
        public void setDominantAgeGroup(String dominantAgeGroup) { this.dominantAgeGroup = dominantAgeGroup; }
        
        public String getDominantFeature() { return dominantFeature; }
        public void setDominantFeature(String dominantFeature) { this.dominantFeature = dominantFeature; }
        
        public String getDominantPersonality() { return dominantPersonality; }
        public void setDominantPersonality(String dominantPersonality) { this.dominantPersonality = dominantPersonality; }
        
        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    }

    /**
     * 추천 근거 정보 생성
     */
    private FamilyPlanRecommendationResponse.RecommendationReason generateRecommendationReason(
            List<FamilyMemberSurveyInfo> allMembers, 
            List<FamilyMemberSurveyInfo> membersWithSurvey) {

        // 1. 주요 특성 분석
        Map<String, Integer> featureCount = new HashMap<>();
        for (FamilyMemberSurveyInfo member : membersWithSurvey) {
            if (member.getFeature() != null) {
                featureCount.put(member.getFeature(), 
                        featureCount.getOrDefault(member.getFeature(), 0) + 1);
            }
        }

        List<String> dominantFeatures = featureCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 2. 가족 유형 결정
        String familyType = determineFamilyType(dominantFeatures);

        // 3. 추천 요약 생성
        String recommendationSummary = generateRecommendationSummary(
                allMembers.size(), 
                membersWithSurvey.size(), 
                familyType
        );

        return new FamilyPlanRecommendationResponse.RecommendationReason(
                allMembers.size(),
                membersWithSurvey.size(),
                dominantFeatures,
                familyType,
                recommendationSummary
        );
    }

    /**
     * 가족 유형 결정 (LG U+ 요금제 특성에 맞춤)
     */
    private String determineFamilyType(List<String> dominantFeatures) {
        if (dominantFeatures.isEmpty()) {
            return "균형형 가족";
        }

        String topFeature = dominantFeatures.get(0);
        
        // LG U+ 요금제 특성에 맞는 가족 유형 분류
        if (topFeature.contains("데이터") || topFeature.contains("인터넷") || topFeature.contains("스트리밍")) {
            return "데이터 헤비 가족";
        } else if (topFeature.contains("통화") || topFeature.contains("음성") || topFeature.contains("전화")) {
            return "통화 중심 가족";
        } else if (topFeature.contains("미디어") || topFeature.contains("엔터테인먼트") || topFeature.contains("콘텐츠")) {
            return "미디어 활용 가족";
        } else if (topFeature.contains("경제") || topFeature.contains("절약") || topFeature.contains("실용")) {
            return "경제적 실용 가족";
        } else if (topFeature.contains("프리미엄") || topFeature.contains("고급") || topFeature.contains("다양")) {
            return "프리미엄 추구 가족";
        } else if (topFeature.contains("안정") || topFeature.contains("신뢰") || topFeature.contains("기본")) {
            return "안정성 중시 가족";
        } else {
            return "다양한 니즈 가족";
        }
    }

    /**
     * 추천 요약 생성
     */
    private String generateRecommendationSummary(int totalMembers, int membersWithSurvey, String familyType) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("총 %d명의 가족 구성원 중 %d명이 설문을 완료했습니다. ", 
                totalMembers, membersWithSurvey));
        
        if (membersWithSurvey == totalMembers) {
            // 모든 구성원이 설문 완료한 경우
            summary.append(String.format("분석 결과 '%s'으로 분류되어 이에 맞는 최적의 요금제를 추천합니다.", familyType));
        } else {
            // 일부 구성원이 설문 미완료한 경우
            int incompleteMembers = totalMembers - membersWithSurvey;
            summary.append(String.format("현재 '%s'으로 분류되어 이에 맞는 요금제를 추천합니다. ", familyType));
            summary.append(String.format("나머지 %d명의 설문이 완료되면 더욱 정확하고 개인화된 추천을 받으실 수 있습니다.", incompleteMembers));
        }
        
        return summary.toString();
    }

    /**
     * 결합 상품 추천 계산
     */
    private FamilyPlanRecommendationResponse.CombinationRecommendation calculateCombinationRecommendation(
            List<FamilyMemberSurveyInfo> allMembers, 
            List<FamilyMemberSurveyInfo> membersWithSurvey) {
        
        int memberCount = allMembers.size();
        
        // 1. 연령대 분석 (청소년 할인 적용 여부)
        int teenCount = 0;
        for (FamilyMemberSurveyInfo member : allMembers) {
            if (member.getAge() != null && 
                (member.getAge().contains("10대") || member.getAge().contains("청소년"))) {
                teenCount++;
            }
        }
        
        // 2. 가족 특성 분석
        FamilyCharacteristics familyChar = analyzeFamilyCharacteristics(membersWithSurvey);
        
        // 3. 각 결합 상품별 할인 계산
        List<FamilyPlanRecommendationResponse.CombinationDetail> availableCombinations = new ArrayList<>();
        
        // 투게더 결합 (인원수 기반 할인)
        int togetherSavings = calculateTogetherCombination(memberCount, teenCount);
        availableCombinations.add(new FamilyPlanRecommendationResponse.CombinationDetail(
                "투게더 결합",
                "인원수에 따라 할인되는 제도 (청소년, 시그니처 가족 할인 포함)",
                togetherSavings,
                String.format("가족 %d명 + 청소년 %d명", memberCount, teenCount),
                false
        ));
        
        // 참쉬운 가족 결합 (휴대폰+인터넷)
        int familyEasySavings = calculateFamilyEasyCombination(memberCount);
        availableCombinations.add(new FamilyPlanRecommendationResponse.CombinationDetail(
                "참쉬운 가족 결합",
                "휴대폰+인터넷 결합할인 (최대 휴대폰 10대, 인터넷 3대 결합가능)",
                familyEasySavings,
                String.format("휴대폰 %d대 + 인터넷 결합", memberCount),
                false
        ));
        
        // 가족무한사랑 (가족 골고루 할인)
        int unlimitedLoveSavings = calculateUnlimitedLoveCombination(memberCount);
        availableCombinations.add(new FamilyPlanRecommendationResponse.CombinationDetail(
                "가족무한사랑",
                "가족이 골고루 할인 받는 방법",
                unlimitedLoveSavings,
                String.format("가족 %d명 균등 할인", memberCount),
                false
        ));
        
        // 참쉬운 케이블 가족 결합
        int cableFamilySavings = calculateCableFamilyCombination(memberCount);
        availableCombinations.add(new FamilyPlanRecommendationResponse.CombinationDetail(
                "참쉬운 케이블 가족 결합",
                "케이블TV + 휴대폰 결합 할인",
                cableFamilySavings,
                String.format("케이블TV + 휴대폰 %d대", memberCount),
                false
        ));
        
        // 4. 최적 결합 상품 선택
        FamilyPlanRecommendationResponse.CombinationDetail bestCombination = availableCombinations.stream()
                .max(Comparator.comparingInt(FamilyPlanRecommendationResponse.CombinationDetail::getMonthlySavings))
                .orElse(availableCombinations.get(0));
        
        bestCombination.setRecommended(true);
        
        // 5. 할인율 계산 (예상 월 요금 대비)
        int estimatedMonthlyBill = memberCount * 70000; // 평균 요금제 기준
        double discountRate = (double) bestCombination.getMonthlySavings() / estimatedMonthlyBill * 100;
        
        // 6. 절약 내역 생성
        String savingsBreakdown = generateSavingsBreakdown(bestCombination, memberCount, teenCount);
        
        // 7. 추천 이유 생성
        String recommendationReason = generateCombinationRecommendationReason(
                bestCombination, familyChar, memberCount, teenCount);
        
        return new FamilyPlanRecommendationResponse.CombinationRecommendation(
                bestCombination.getCombinationName(),
                bestCombination.getMonthlySavings(),
                savingsBreakdown,
                String.format("%.1f%%", discountRate),
                recommendationReason,
                availableCombinations
        );
    }
    
    /**
     * 투게더 결합 할인 계산
     */
    private int calculateTogetherCombination(int memberCount, int teenCount) {
        // 기본 인당 할인: 14,000원
        int basicDiscount = memberCount * 14000;
        
        // 청소년 추가 할인: 10,000원 per 청소년
        int teenDiscount = teenCount * 10000;
        
        // 최대 5명까지 할인 적용
        int maxMembers = Math.min(memberCount, 5);
        
        return (maxMembers * 14000) + teenDiscount;
    }
    
    /**
     * 참쉬운 가족 결합 할인 계산
     */
    private int calculateFamilyEasyCombination(int memberCount) {
        // 휴대폰 + 인터넷 결합 시 할인
        // 휴대폰 1대당 8,000원 할인 + 인터넷 할인 15,000원
        int phoneDiscount = Math.min(memberCount, 10) * 8000; // 최대 10대
        int internetDiscount = 15000; // 인터넷 기본 할인
        
        return phoneDiscount + internetDiscount;
    }
    
    /**
     * 가족무한사랑 할인 계산
     */
    private int calculateUnlimitedLoveCombination(int memberCount) {
        // 가족 구성원 수에 따른 균등 할인
        if (memberCount >= 4) {
            return memberCount * 12000; // 4명 이상 시 인당 12,000원
        } else if (memberCount >= 3) {
            return memberCount * 10000; // 3명 시 인당 10,000원
        } else {
            return memberCount * 8000;  // 2명 이하 시 인당 8,000원
        }
    }
    
    /**
     * 참쉬운 케이블 가족 결합 할인 계산
     */
    private int calculateCableFamilyCombination(int memberCount) {
        // 케이블TV + 휴대폰 결합
        int cableDiscount = 20000; // 케이블TV 기본 할인
        int phoneDiscount = Math.min(memberCount, 8) * 7000; // 휴대폰 1대당 7,000원 (최대 8대)
        
        return cableDiscount + phoneDiscount;
    }
    
    /**
     * 절약 내역 상세 생성
     */
    private String generateSavingsBreakdown(
            FamilyPlanRecommendationResponse.CombinationDetail bestCombination, 
            int memberCount, int teenCount) {
        
        StringBuilder breakdown = new StringBuilder();
        
        switch (bestCombination.getCombinationName()) {
            case "투게더 결합":
                breakdown.append(String.format("• 인당 기본 할인: 14,000원 × %d명 = %,d원\n", 
                        Math.min(memberCount, 5), Math.min(memberCount, 5) * 14000));
                if (teenCount > 0) {
                    breakdown.append(String.format("• 청소년 추가 할인: 10,000원 × %d명 = %,d원\n", 
                            teenCount, teenCount * 10000));
                }
                break;
                
            case "참쉬운 가족 결합":
                breakdown.append(String.format("• 휴대폰 할인: 8,000원 × %d대 = %,d원\n", 
                        Math.min(memberCount, 10), Math.min(memberCount, 10) * 8000));
                breakdown.append("• 인터넷 결합 할인: 15,000원\n");
                break;
                
            case "가족무한사랑":
                int perPersonDiscount = memberCount >= 4 ? 12000 : (memberCount >= 3 ? 10000 : 8000);
                breakdown.append(String.format("• 가족 균등 할인: %,d원 × %d명 = %,d원\n", 
                        perPersonDiscount, memberCount, perPersonDiscount * memberCount));
                break;
                
            case "참쉬운 케이블 가족 결합":
                breakdown.append("• 케이블TV 결합 할인: 20,000원\n");
                breakdown.append(String.format("• 휴대폰 할인: 7,000원 × %d대 = %,d원\n", 
                        Math.min(memberCount, 8), Math.min(memberCount, 8) * 7000));
                break;
        }
        
        breakdown.append(String.format("⇒ 총 할인액: %,d원/월", bestCombination.getMonthlySavings()));
        
        return breakdown.toString();
    }
    
    /**
     * 결합 상품 추천 이유 생성
     */
    private String generateCombinationRecommendationReason(
            FamilyPlanRecommendationResponse.CombinationDetail bestCombination,
            FamilyCharacteristics familyChar,
            int memberCount, int teenCount) {
        
        StringBuilder reason = new StringBuilder();
        
        // 기본 추천 이유
        reason.append(String.format("%s 이용 시 한 달에 최대 %,d원 아낄 수 있어요! ", 
                bestCombination.getCombinationName(), bestCombination.getMonthlySavings()));
        
        // 가족 특성에 따른 맞춤 이유
        if (bestCombination.getCombinationName().equals("투게더 결합")) {
            reason.append("가족 구성원이 많을수록 더 많은 할인 혜택을 받을 수 있는 가장 기본적인 결합상품입니다. ");
            if (teenCount > 0) {
                reason.append("특히 청소년 자녀가 있어 추가 할인 혜택까지 받을 수 있어 더욱 경제적입니다. ");
            }
        } else if (bestCombination.getCombinationName().equals("참쉬운 가족 결합")) {
            reason.append("집에서 인터넷을 많이 사용하는 가족에게 최적화된 결합상품으로, 통신비를 크게 절약할 수 있습니다. ");
        } else if (bestCombination.getCombinationName().equals("가족무한사랑")) {
            reason.append("모든 가족 구성원이 균등하게 할인 혜택을 받을 수 있어 공평하고 합리적인 선택입니다. ");
        } else if (bestCombination.getCombinationName().equals("참쉬운 케이블 가족 결합")) {
            reason.append("TV 시청과 모바일 사용을 모두 중시하는 가족에게 완벽한 올인원 패키지입니다. ");
        }
        
        // 경제성 강조
        if (familyChar.getDominantPersonality() != null && 
            familyChar.getDominantPersonality().contains("지갑")) {
            reason.append("경제적 실용성을 중시하는 가족 특성에 가장 적합한 선택입니다!");
        }
        
        return reason.toString();
    }
} 